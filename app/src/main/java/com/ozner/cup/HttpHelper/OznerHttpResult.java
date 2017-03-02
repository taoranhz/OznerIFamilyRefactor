package com.ozner.cup.HttpHelper;

/**
 * Created by ozner_67 on 2017/2/17.
 * 邮箱：xinde.zhang@cftcn.com
 */

public interface OznerHttpResult<T> {
    void onError(Throwable e);

    void onNext(T jsonObject);
}
