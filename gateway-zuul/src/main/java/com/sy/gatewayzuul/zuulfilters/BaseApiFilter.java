package com.sy.gatewayzuul.zuulfilters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.sy.basis.common.BaseResult;
import com.sy.basis.util.ResultUtil;
import com.sy.gatewayzuul.utils.JwtTokenProvider;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;


import java.util.List;


/**
 * @author wangxiao
 * @description: //TODO
 * @date 2020/3/26
 */
public abstract class BaseApiFilter extends ZuulFilter {


    private Logger log = LoggerFactory.getLogger("zuul");




    private ObjectMapper objectMapper = new ObjectMapper();
    private PathMatcher pathMatcher = new AntPathMatcher();

    private List<String> patternList;

    protected JwtTokenProvider jwtTokenProvider;


    public BaseApiFilter(List<String> patternList, JwtTokenProvider jwtTokenProvider) {
        this.patternList = patternList;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public BaseApiFilter() {
    }

    @Override
    public boolean shouldFilter() {
        RequestContext currentContext = RequestContext.getCurrentContext();
        String uri = getUri(currentContext);
        return isPatternList(uri);
    }

    @Override
    public Object run() throws ZuulException {
        try {
            return doFilter();
        }catch (Exception e){
            serverError(e);
            return null;
        }
    }

    /**
     *  拦截器具体实现功能
     * @return
     */
    protected abstract Object doFilter()throws Exception;


    /**
     * 判断是否需要鉴权
     * @param uri
     * @return
     */
    private boolean isPatternList (String uri) {
        for (String patternUrl : patternList){
            if (pathMatcher.match(patternUrl,uri)) {
                return true;
            }
        }
        return false;
    }

    protected String getUri(RequestContext context) {
        return context.getRequest().getRequestURI();
    }


    protected void serverError(Exception e){
        log.info("gateWay-zuul is error ,the message is :{}",e.getMessage());
        sendBaseResponse(ResultUtil.fail("网关服务解析异常"));
    }

    protected void nopermission (String id){
        log.info("gateWay-zuul is no permission ,the request id is :{}",id);
        sendBaseResponse((ResultUtil.fail("您没有相关权限！ 请联系管理员")));
    }

     protected void sendBaseResponse (BaseResult baseResponse) {
         RequestContext context = RequestContext.getCurrentContext();
         context.setSendZuulResponse(false);
         context.setResponseStatusCode(HttpStatus.SC_OK);
         context.getResponse().setContentType(MediaType.APPLICATION_JSON_VALUE);
         context.getResponse().setCharacterEncoding("UTF-8");
         String json = "{\"status:\"fail\"}";
         try  {
            json = objectMapper.writeValueAsString(baseResponse);
         }catch (JsonProcessingException e1) {
             e1.printStackTrace();
         }
         context.setResponseBody(json);

     }





}
