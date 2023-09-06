pub mod modules;


#[macro_export]
macro_rules! java_class {
    (
        $(
            [$package:ident] $name:ident {
                $(
                    fn $method:ident($($arg:ident: $arg_type:ty),* $(,)?) -> $returnable:ty;
                )*
            }
        )*
    ) => {
        paste::paste! {
            $(
                $(
                    #[no_mangle]
                    pub extern "system" fn [<Java_ $package _ $name _ $method>](env: JNIEnv, class: JClass, $($arg: $arg_type),*) -> $returnable {
                        let result = $name::$method(env, class, $($arg),*);
                        return result;
                    }
                )*

                struct $name;

                trait [<$name Trait>] {
                    $(
                        fn $method(_env: JNIEnv, _class: JClass, $($arg: $arg_type),*) -> $returnable;
                    )*
                }
            )*
        }
    };
}
