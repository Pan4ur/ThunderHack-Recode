package dev.thunderhack.utils.render.animation;

import dev.thunderhack.utils.Timer;

public class Animation {

    public Timer timerUtil = new Timer();
    protected int duration;
    protected double endPoint;
    protected Direction direction;

    public Animation(int ms, double endPoint) {
        this.duration = ms;
        this.endPoint = endPoint;
        this.direction = Direction.FORWARDS;
    }

    public Animation(int ms, double endPoint, Direction direction) {
        this.duration = ms;
        this.endPoint = endPoint;
        this.direction = direction;
    }

    public boolean finished(Direction direction) {
        return isDone() && this.direction.equals(direction);
    }

    public void reset() {
        timerUtil.reset();
    }

    public boolean isDone() {
        return timerUtil.passedMs(duration);
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        if (this.direction != direction) {
            this.direction = direction;
            timerUtil.setMs(System.currentTimeMillis() - (duration - Math.min(duration, timerUtil.getPassedTimeMs())));
        }
    }
    protected boolean correctOutput() {
        return false;
    }

    public double getOutput() {
        if (direction == Direction.FORWARDS) {
            if (isDone())
                return endPoint;
            return (getEquation(timerUtil.getPassedTimeMs()) * endPoint);
        } else {
            if (isDone()) return 0;
            if (correctOutput()) {
                double revTime = Math.min(duration, Math.max(0, duration - timerUtil.getPassedTimeMs()));
                return getEquation(revTime) * endPoint;
            } else return (1 - getEquation(timerUtil.getPassedTimeMs())) * endPoint;
        }
    }

    protected double getEquation(double x) {
        return 0;
    }
}
