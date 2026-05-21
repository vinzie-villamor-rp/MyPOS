package com.rocketpartners.pos.event;

import java.util.ArrayList;
import java.util.List;

public class PosEventDispatcher {

    private final List<PosEventListener> listeners = new ArrayList<>();

    public void addListener(PosEventListener listener) {
        listeners.add(listener);
    }

    public void dispatch(PosEvent event, Object payload) {
        for (PosEventListener listener : listeners) {
            listener.onPosEvent(event, payload);
        }
    }

    public void dispatch(PosEvent event) {
        dispatch(event, null);
    }
}
