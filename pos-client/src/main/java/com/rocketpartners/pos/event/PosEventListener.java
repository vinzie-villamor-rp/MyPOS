package com.rocketpartners.pos.event;

public interface PosEventListener {

    void onPosEvent(PosEvent event, Object payload);
}
