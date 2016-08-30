package com.ksy.recordlib.service.core;

/**
 * Created by huping on 2016/8/17.
 */
public interface Throughput {

    /**
     * Monitor
     *
     * @return
     */
    ThroughputStatistic monitorThroughput();

    /**
     * flush
     *
     * @return
     */
    void flushQueue();
}
