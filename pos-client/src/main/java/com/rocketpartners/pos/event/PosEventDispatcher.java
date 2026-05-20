package com.rocketpartners.pos.event;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class PosEventDispatcher {

    private final List<PosEventListener> listeners;

    public void dispatch(PosEvent event, Object payload) {
        for (PosEventListener listener : listeners) {
            listener.onPosEvent(event, payload);
        }
    }

    public void dispatch(PosEvent event) {
        dispatch(event, null);
    }
}
