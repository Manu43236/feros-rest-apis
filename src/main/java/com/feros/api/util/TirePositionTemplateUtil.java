package com.feros.api.util;

import com.feros.api.enums.TirePositionType;

import java.util.ArrayList;
import java.util.List;

public class TirePositionTemplateUtil {

    public record PositionDef(String code, TirePositionType type, int order) {}

    /**
     * Returns standard tire position definitions for a given tyre count.
     *
     * Patterns:
     *   4  tires → 2 steer (FL/FR) + 2 single rear (RL/RR) + SPARE
     *   6  tires → 2 steer + 1 dual rear axle (4 tires) + SPARE
     *  10  tires → 2 steer + 2 dual rear axles + SPARE
     *  14  tires → 2 steer + 3 dual rear axles + SPARE
     *  18  tires → 2 steer + 4 dual rear axles + SPARE
     *  22  tires → 2 steer + 5 dual rear axles + SPARE
     *  26  tires → 2 steer + 6 dual rear axles + SPARE
     *  30  tires → 2 steer + 7 dual rear axles + SPARE
     *  34  tires → 2 steer + 8 dual rear axles + SPARE
     *  36  tires → 4 steer (double-steer) + 8 dual rear axles + SPARE
     *  other    → best-effort using floor((n-2)/4) dual rear axles
     */
    public static List<PositionDef> getTemplate(int tyreCount) {
        if (tyreCount == 4)  return template4();
        if (tyreCount == 36) return template36();
        int rearAxles = Math.max(1, (tyreCount - 2) / 4);
        return templateDualRear(rearAxles);
    }

    // ── 4-tire: single rear wheels ────────────────────────────────────────────

    private static List<PositionDef> template4() {
        List<PositionDef> list = new ArrayList<>();
        list.add(new PositionDef("FL",    TirePositionType.STEER, 1));
        list.add(new PositionDef("FR",    TirePositionType.STEER, 2));
        list.add(new PositionDef("RL",    TirePositionType.DRIVE, 3));
        list.add(new PositionDef("RR",    TirePositionType.DRIVE, 4));
        list.add(new PositionDef("SPARE", TirePositionType.SPARE, 5));
        return list;
    }

    // ── Standard: 2 steer + n dual rear axles + SPARE ─────────────────────────
    //    Each axle → Left-Outer, Left-Inner, Right-Outer, Right-Inner

    private static List<PositionDef> templateDualRear(int rearAxles) {
        List<PositionDef> list = new ArrayList<>();
        list.add(new PositionDef("FL", TirePositionType.STEER, 1));
        list.add(new PositionDef("FR", TirePositionType.STEER, 2));

        int order = 3;
        for (int axle = 1; axle <= rearAxles; axle++) {
            String prefix = "R" + axle;
            list.add(new PositionDef(prefix + "L-O", TirePositionType.DRIVE, order++));
            list.add(new PositionDef(prefix + "L-I", TirePositionType.DRIVE, order++));
            list.add(new PositionDef(prefix + "R-O", TirePositionType.DRIVE, order++));
            list.add(new PositionDef(prefix + "R-I", TirePositionType.DRIVE, order++));
        }

        list.add(new PositionDef("SPARE", TirePositionType.SPARE, order));
        return list;
    }

    // ── 36-tire: double-steer front + 8 dual rear axles ──────────────────────

    private static List<PositionDef> template36() {
        List<PositionDef> list = new ArrayList<>();
        list.add(new PositionDef("A1L",   TirePositionType.STEER, 1));
        list.add(new PositionDef("A1R",   TirePositionType.STEER, 2));
        list.add(new PositionDef("A2L",   TirePositionType.STEER, 3));
        list.add(new PositionDef("A2R",   TirePositionType.STEER, 4));

        int order = 5;
        for (int axle = 1; axle <= 8; axle++) {
            String prefix = "R" + axle;
            list.add(new PositionDef(prefix + "L-O", TirePositionType.DRIVE, order++));
            list.add(new PositionDef(prefix + "L-I", TirePositionType.DRIVE, order++));
            list.add(new PositionDef(prefix + "R-O", TirePositionType.DRIVE, order++));
            list.add(new PositionDef(prefix + "R-I", TirePositionType.DRIVE, order++));
        }

        list.add(new PositionDef("SPARE", TirePositionType.SPARE, order));
        return list;
    }
}
