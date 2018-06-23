package nybsys.tillboxweb.enumpurches;

import java.util.Map;
import java.util.TreeMap;

public enum AdjustmentEffect {

    Increase(1),
    Decrease(2);


    private Integer adjustmentEffect;

    AdjustmentEffect(Integer adjustmentEffect) {
        this.adjustmentEffect = adjustmentEffect;
    }

    public Integer get() {
        return this.adjustmentEffect;
    }

    private static final Map<String, Integer> MAP = new TreeMap<>();

    static {
        for (AdjustmentEffect kv : AdjustmentEffect.values()) {
            MAP.put(kv.name(), kv.get());
        }
    }

    public static Map<String, Integer> getMAP() {
        return MAP;
    }
}