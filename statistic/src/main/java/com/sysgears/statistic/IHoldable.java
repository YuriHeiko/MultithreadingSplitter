package com.sysgears.statistic;

import java.util.Map;

public interface IHoldable<T, U> {
    U add(T id, U value);

    U get(T id);

    U remove(T id);

    Map<T, U> getAll();
}