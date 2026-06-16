package com.bigyun.provider.db.runtime;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import java.util.function.Supplier;

/**
 * Provider DB data source switch helper.
 */
public final class ProviderRuntimeDataSource
{
    private static final String MASTER = "master";

    private static final String SLAVE = "slave";

    private ProviderRuntimeDataSource()
    {
    }

    public static <T> T readFromSlave(Supplier<T> supplier)
    {
        return use(SLAVE, supplier);
    }

    public static <T> T readFromMaster(Supplier<T> supplier)
    {
        return use(MASTER, supplier);
    }

    private static <T> T use(String dataSource, Supplier<T> supplier)
    {
        DynamicDataSourceContextHolder.push(dataSource);
        try
        {
            return supplier.get();
        }
        finally
        {
            DynamicDataSourceContextHolder.poll();
        }
    }
}
