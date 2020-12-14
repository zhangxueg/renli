package com.renli.common.exception.user;

import com.renli.common.exception.BaseException;

/**
 * 用户信息异常类
 * 
 * @author zhangxuegang
 */
public class UserException extends BaseException
{
    private static final long serialVersionUID = 1L;

    public UserException(String code, Object[] args)
    {
        super("user", code, args, null);
    }
}
