package com.sysgears.processor.statistic;

import com.sysgears.processor.exceptions.StatisticHolderException;

public class StatisticHandler implements Runnable {
    StatisticHolder statisticHolder;

    public StatisticHandler(StatisticHolder statisticHolder) {
        this.statisticHolder = statisticHolder;
    }

    @Override
    public void run() {
        while (!statisticHolder.isJobDone()) {
            System.out.println(statisticHolder.toString());

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new StatisticHolderException("The statistic thread has been suddenly interrupted!");
            }
        }

        System.out.println(statisticHolder.toString());
        System.out.println("All tasks have been done.");
    }
}
