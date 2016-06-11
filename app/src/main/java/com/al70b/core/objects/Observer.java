package com.al70b.core.objects;

import android.util.Log;
import android.util.SparseArray;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Created by Naseem on 5/31/2015.
 */
public abstract class Observer {
    private SparseArray<Boolean> conditions;
    private boolean stopObserverBoolean;

    private Field[] conditionsNames;

    private boolean lastEval, prevEval;


    public Observer(Class aClass) {
        conditions = new SparseArray<>();

        try {
            conditionsNames = aClass.getDeclaredFields();

            // initialize conditions names and each one assign to false
            for (Field f : conditionsNames)
                if (f.getType().equals(int.class) && Modifier.isStatic(f.getModifiers())) {
                    f.setAccessible(true);
                    conditions.put(f.getInt(null), false);
                }
        } catch (Exception ex) {
            Log.d("problem", ex.toString());
        }
    }

    public boolean evaluateCondition() {
        // get last evaluation
        prevEval = lastEval;

        // evaluate condition
        lastEval = evalCondition();

        if (lastEval && prevEval)
            return lastEval;

        if (lastEval && !stopObserverBoolean) {
            if (!prevEval)
                onEvalConditionTrue();
        } else {
            onEvalConditionFalse();
        }

        return lastEval;
    }

    public void setCondition(int condName, boolean flag) {
        conditions.remove(condName);
        conditions.put(condName, flag);
        evaluateCondition();
    }

    protected boolean getCondition(int condName) {
        return conditions.get(condName);
    }

    public boolean getEvaluatedCondition() {
        return lastEval;
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
