package org.patryk3211.powergrid.electricity.solarpanel;

import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollValueBehaviour;

import java.util.ArrayList;
import java.util.List;

public class SolarPanelBearingBlockScrollBehaviour extends ServerScrollValueBehaviour {
    private List<Integer> divisors = new ArrayList<>(List.of(1));
    public int panelCount = 1;

    public SolarPanelBearingBlockScrollBehaviour(SmartBlockEntity be) {
        super(be);
        between(0, 0);
        value = 0;
    }

    public List<Integer> getDivisors() {
        return divisors;
    }

    public int getPanelCount() {
        return panelCount;
    }

    public void refreshDivisors(int panelCount) {
        this.panelCount = panelCount;
        divisors = new ArrayList<>();
        for (int i = 1; i <= 9; i++) {
            if (panelCount % i == 0 && (panelCount / i) <= 25)
                divisors.add(i);
        }
        int maxIdx = Math.max(0, divisors.size() - 1);
        between(0, maxIdx);
        if (value > maxIdx)
            value = maxIdx;
    }

    public int getDivisor() {
        if (divisors.isEmpty())
            return 1;
        int idx = Math.max(0, Math.min(value, divisors.size() - 1));
        return divisors.get(idx);
    }

    public void setByDivisor(int savedDivisor) {
        for (int i = 0; i < divisors.size(); i++) {
            if (divisors.get(i) == savedDivisor) {
                value = i;
                return;
            }
        }
        value = 0;
    }
}
