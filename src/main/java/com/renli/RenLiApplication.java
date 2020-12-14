package com.renli;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 启动程序
 * 
 * @author zhangxuegang
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class RenLiApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(RenLiApplication.class, args);
        System.out.println(" 项目启动成功");
    }
}
