package de.petanqueturniermanager.sidebar.layout;

import java.util.LinkedHashMap;
import java.util.Map;

import com.sun.star.awt.Rectangle;

/**
 * Ein horizontales Layout. Alle enthaltenen Layouts werden in einer Reihe angezeigt. Die Breite wird dynamisch an Hand der
 * Gewichtung berechnet.
 *
 * @author daniel.sikeler
 */
public class HorizontalLayout implements Layout {

    private final int LEFT_RIGHT_BORDER = 5;
    private int marginBetween = 1;

    /**
     * Container für die enthaltenen Layouts.<br>
     * Layout + Gewichtung
     */
    // Collections.synchronizedMap
    private Map<Layout, Integer> layouts = new LinkedHashMap<>();

    @Override
    public int layout(Rectangle rect) {
        int xOffset = LEFT_RIGHT_BORDER;
        int height = 0;

        // zwischenraum von 1 px nur zwischen den elementen
        int gesMargin = (layouts.size() - 1) * marginBetween;
        int summeFixWidth = layouts.keySet().parallelStream().filter(key -> key instanceof ControlLayout)
                .map(key -> ((ControlLayout) key).getFixWidth()).reduce(0, Integer::sum);
        int widthOhneFixUndMarginUndBorder = Math.max(rect.Width - summeFixWidth - gesMargin - (LEFT_RIGHT_BORDER * 2), 0); // nicht kleiner als 0
        int widthProGewichtung = widthOhneFixUndMarginUndBorder / layouts.values().stream().reduce(0, Integer::sum); // width / addierten Gewichtungen

        for (Map.Entry<Layout, Integer> entry : layouts.entrySet()) {
            int newWidth = widthProGewichtung * entry.getValue();
            if (entry.getKey() instanceof ControlLayout && ((ControlLayout) entry.getKey()).getFixWidth() > 0) {
                newWidth = ((ControlLayout) entry.getKey()).getFixWidth();
            }

            height = Integer.max(height, entry.getKey().layout(new Rectangle(rect.X + xOffset, rect.Y, newWidth, rect.Height)));
            xOffset += newWidth;
            xOffset += marginBetween;
        }

        return height;
    }

    @Override
    public void addLayout(Layout layout, int width) {
        layouts.put(layout, width);
    }

    @Override
    public int getHeight() {
        return layouts.keySet().stream().mapToInt(Layout::getHeight).max().orElse(0);
    }

}
