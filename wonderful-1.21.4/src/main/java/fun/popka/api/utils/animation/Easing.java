package fun.popka.api.utils.animation;

@FunctionalInterface
public interface Easing {
    double ease(double value);
}