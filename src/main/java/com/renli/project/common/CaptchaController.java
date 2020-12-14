package com.renli.project.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.renli.common.constant.Constants;
import com.renli.common.utils.IdUtils;
import com.renli.common.utils.VerifyCodeUtils;
import com.renli.common.utils.sign.Base64;
import com.renli.framework.redis.RedisCache;
import com.renli.framework.web.domain.AjaxResult;

/**
 * 验证码操作处理
 * 
 * @author zhangxuegang
 */
@RestController
public class CaptchaController
{
    @Autowired
    private RedisCache redisCache;

    /**
     * 生成验证码
     */
    @GetMapping("/captchaImage")
    public AjaxResult getCode(HttpServletResponse response) throws IOException
    {
        // 生成随机字串
        String verifyCode = VerifyCodeUtils.generateVerifyCode(4);
        // 唯一标识
        String uuid = IdUtils.simpleUUID();
        String verifyKey = Constants.CAPTCHA_CODE_KEY + uuid;

        redisCache.setCacheObject(verifyKey, verifyCode, Constants.CAPTCHA_EXPIRATION, TimeUnit.MINUTES);
        // 生成图片
        int w = 111, h = 36;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        VerifyCodeUtils.outputImage(w, h, stream, verifyCode);
        try
        {
            AjaxResult ajax = AjaxResult.success();
            ajax.put("uuid", uuid);
            ajax.put("img", Base64.encode(stream.toByteArray()));
            return ajax;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }
        finally
        {
            stream.close();
        }
    }
}
