package com.al70b.core.objects;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Naseem on 5/31/2015.
 */
public abstract class ObserverBackup<T extends Enum> {
    private Map<T, Boolean> conditions;
    private boolean stopObserverBoolean;

    private T[] conditionsNames;
    private boolean evalCondition;


    public ObserverBackup(Class enumClass) {
        conditionsNames = (T[]) enumClass.getEnumConstants();
        conditions = new HashMap<>();

        // initialize conditions names and each one assign to false
        for (T t : conditionsNames)
            conditions.put(t, false);
    }

    public boolean evaluateCondition() {
        boolean temp = evalCondition();
        if (evalCondition && temp)
            return evalCondition();

        evalCondition = temp;

        if (evalCondition && !stopObserverBoolean) {
            onEvalConditionTrue();
        } else {
            onEvalConditionFalse();
        }

        return evalCondition;
    }

    public void setCondition(T condName, boolean flag) {
        conditions.remove(condName);
        conditions.put(condName, flag);
        evaluateCondition();
    }

    protected boolean getCondition(T condName) {
        return conditions.get(condName);
    }

    public boolean getEvaluatedCondition() {
        return evalCondition;
    }

    public void stopObserver() {
        stopObserverBoolean = true;
        evalCondition();
    }

    public void startObserver() {
        stopObserverBoolean = false;
        evalCondition();
    }

    public boolean isObserving() {
        return !stopObserverBoolean;
    }

    public abstract boolean evalCondition();

    public abstract void onEvalConditionTrue();

    public abstract void onEvalConditionFalse();

}
