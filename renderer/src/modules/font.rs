#![allow(non_snake_case)]

use std::collections::HashMap;
use std::ffi::c_void;
use fontdue::Font;
use jni::JNIEnv;
use jni::objects::{JByteArray, JClass, JObject, JString, JValue};
use jni::sys::{jboolean, jchar, jfloat, jint, jstring};
use once_cell::unsync::Lazy;
use crate::java_class;

java_class! {
    [thunder_hack_utility_render_font] NativeFontRasterizer {
        fn nativeSetup(callback: JObject) -> ();
        fn nativeLoadFont(font: JByteArray, name: JString) -> jint;
        fn nativeGetGlyphCount(font: jint) -> jint;
        fn nativeGetName(font: jint) -> jstring;
        fn nativeSearchFont(name: JString) -> jint;
        fn nativeDeleteFont(font: jint) -> jboolean;
        fn nativeMakeAtlas(font: jint, size: jfloat, char_segment: jint, callback: JObject, texture_id: jint) -> jboolean;
        fn nativeGetLineMetrics(font: jint, size: jfloat, horizontal: jboolean, callback: JObject) -> jboolean;
    }
}

struct LoadedFont {
    font: Font,
    name: String
}

static mut FONTS: Lazy<HashMap<jint, LoadedFont>> = Lazy::new(HashMap::new);
static mut FONT_ID: Lazy<jint> = Lazy::new(|| 0);

impl NativeFontRasterizerTrait for NativeFontRasterizer {
    fn nativeSetup(mut env: JNIEnv, _class: JClass, callback: JObject) {
        println!("Setting up font rasterizer");
        let callback = env.new_global_ref(callback).unwrap();
        gl::load_with(|symbol| {
            let symbol = env.new_string(symbol).unwrap();
            env.call_method(callback.as_obj(), "getGlFunction", "(Ljava/lang/String;)J", &[(&symbol).into()]).unwrap().j().unwrap() as *const c_void
        });
    }

    fn nativeLoadFont(mut env: JNIEnv, _class: JClass, font: JByteArray, name: JString) -> jint {
        let font_result = Font::from_bytes(env.convert_byte_array(font).unwrap(), Default::default());
        match font_result {
            Ok(result) => {
                let loaded = LoadedFont {
                    font: result,
                    name: env.get_string(&name).unwrap().into()
                };
                unsafe {
                    *FONT_ID += 1;
                    let identifier = *FONT_ID;
                    FONTS.insert(identifier, loaded);
                    identifier
                }
            }
            Err(err) => {
                println!("{}", &format!("Error loading font: {}", err));
                -1
            }
        }
    }

    fn nativeGetGlyphCount(_env: JNIEnv, _class: JClass, font: jint) -> jint {
        unsafe {
            let loaded_font = FONTS.get(&font);
            if loaded_font.is_none() {
                println!("{}", &format!("Error getting glyph count: Font with id {} does not exist", font.to_string()));
                return -1;
            }
            let count = loaded_font.unwrap().font.glyph_count();
            count as jint
        }
    }

    fn nativeGetName(env: JNIEnv, _class: JClass, font: jint) -> jstring {
        unsafe {
            let loaded_font = FONTS.get(&font);
            if loaded_font.is_none() {
                println!("{}", &format!("Error getting font name: Font with id {} does not exist", font.to_string()));
                return env.new_string("").unwrap().as_raw();
            }
            let name = loaded_font.unwrap().name.clone();
            let name = env.new_string(name).unwrap();
            name.as_raw()
        }
    }

    fn nativeSearchFont(mut env: JNIEnv, _class: JClass, name: JString) -> jint {
        unsafe {
            let name: String = env.get_string(&name).unwrap().into();
            for (id, font) in FONTS.iter() {
                if font.name == name {
                    return *id;
                }
            }
            -1
        }
    }

    fn nativeDeleteFont(_env: JNIEnv, _class: JClass, font: jint) -> jboolean {
        unsafe {
            let success = FONTS.remove(&font).is_some();
            if !success {
                println!("{}", &format!("Error deleting font: Font with id {} does not exist", font.to_string()));
            }
            FONTS.remove(&font).is_some() as jboolean
        }
    }

    fn nativeMakeAtlas(mut env: JNIEnv, _class: JClass, font: jint, size: jfloat, char_segment: jint, callback: JObject, texture_id: jint) -> jboolean {
        let callback = env.new_global_ref(callback).unwrap();

        let loaded_font = unsafe {
            let loaded_font = FONTS.get(&font);
            if loaded_font.is_none() {
                println!("{}", &format!("Error getting font name: Font with id {} does not exist", font.to_string()));
                return false as jboolean;
            }
            loaded_font.unwrap()
        };
        let char_range = (char_segment << 7) as u32..((char_segment + 1) << 7) as u32;
        let font = &loaded_font.font;
        let mut glyphs = Vec::new();
        let (mut max_height, mut total_width) = (0, 0);
        unsafe {
            for char_code in char_range {
                let ch = char::from_u32_unchecked(char_code);
                let glyph = font.rasterize(ch, size);
                if glyph.0.height > max_height {
                    max_height = glyph.0.height;
                }
                total_width += glyph.0.width;
                glyphs.push((ch, glyph.0, glyph.1));
            }
        }

        let mut height_computed = false;
        let canvas_width;
        let mut canvas_height = 0;
        let mut i: u32 = 0;
        loop {
            i += 1;
            if !height_computed {
                if 2usize.pow(i) >= max_height {
                    canvas_height = 2usize.pow(i) as u32;
                    height_computed = true;
                    i = 0;
                }
            } else if 2usize.pow(i) >= total_width + 127 {
                canvas_width = 2usize.pow(i) as u32;
                break;
            }
        }

        let mut bytes = vec![0u8; (canvas_width * canvas_height) as usize];

        let mut x: usize = 0;
        for (char, metrics, coverage) in glyphs {
            let width = metrics.width;
            for (idx, pixel) in coverage.iter().enumerate() {
                let y = idx / width;
                let x = x + idx % width;
                bytes[y * canvas_width as usize + x] = *pixel;
            }
            env.call_method(
                callback.clone(),
                "pushCharData",
                "(CIIIIIIFFFFFF)V",
                &[
                    JValue::from(char as jchar),
                    JValue::from(x as jint),
                    JValue::from(0 as jint),
                    JValue::from(metrics.width as jint),
                    JValue::from(metrics.height as jint),
                    JValue::from(metrics.xmin as jint),
                    JValue::from(metrics.ymin as jint),
                    JValue::from(metrics.advance_width as jfloat),
                    JValue::from(metrics.advance_height as jfloat),
                    JValue::from(metrics.bounds.xmin as jfloat),
                    JValue::from(metrics.bounds.ymin as jfloat),
                    JValue::from(metrics.bounds.width as jfloat),
                    JValue::from(metrics.bounds.height as jfloat),
                ],
            ).unwrap();
            x += metrics.width;
        }

        unsafe {
            gl::BindTexture(gl::TEXTURE_2D, texture_id as u32);
            gl::PixelStorei(gl::UNPACK_ROW_LENGTH, canvas_width as i32);
            gl::PixelStorei(gl::UNPACK_SKIP_PIXELS, 0);
            gl::PixelStorei(gl::UNPACK_SKIP_ROWS, 0);
            gl::TexImage2D(gl::TEXTURE_2D, 0, gl::R8 as gl::types::GLint, canvas_width as i32, canvas_height as i32, 0, gl::RED, gl::UNSIGNED_BYTE, bytes.as_ptr() as *const c_void);
            gl::BindTexture(gl::TEXTURE_2D, 0);
        }

        drop(bytes);

        true as jboolean
    }

    fn nativeGetLineMetrics(mut env: JNIEnv, _class: JClass, font: jint, size: jfloat, horizontal: jboolean, callback: JObject) -> jboolean {
        unsafe {
            let loaded_font = FONTS.get(&font);
            if loaded_font.is_none() {
                println!("{}", &format!("Error getting font name: Font with id {} does not exist", font));
                return false as jboolean;
            }
            let font = &loaded_font.unwrap().font;
            let metrics = if horizontal != 0 { font.horizontal_line_metrics(size).unwrap() } else { font.vertical_line_metrics(size).unwrap() };
            env.call_method(
                callback,
                "pushLineMetrics",
                "(FFFF)V",
                &[
                    JValue::from(metrics.ascent as jfloat),
                    JValue::from(metrics.descent as jfloat),
                    JValue::from(metrics.line_gap as jfloat),
                    JValue::from(metrics.new_line_size as jfloat),
                ],
            ).unwrap();
        }
        true as jboolean
    }
}