package com.example.demo.config.listner;

import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.r2dbc.proxy.core.Bindings;
import io.r2dbc.proxy.core.QueryExecutionInfo;
import io.r2dbc.proxy.core.QueryInfo;
import io.r2dbc.proxy.listener.ProxyExecutionListener;

public class InlineQueryLoggingListener {
//implements ProxyExecutionListener {
	
	private static final Logger log = LoggerFactory.getLogger(InlineQueryLoggingListener.class);

//    @Override
//    public void afterQuery(QueryExecutionInfo execInfo) {
//        Duration time = execInfo.getExecuteDuration();
//        String connId = String.valueOf(execInfo.getConnectionInfo().getConnectionId());
//        
//        //1개 쿼리만 보여줌( 트랜잭션으로 여러개 돌렸을땐 맨처음 1개만 보여줄듯
//        
//        // 원본 SQL
//        List<QueryInfo> queries = execInfo.getQueries();
//        if (queries.isEmpty()) {
//            return;
//        }
//        String sql = queries.get(0).getQuery();
//        
//        // 바인딩 값 (여러 바인딩 세트 중 첫번째만 표시)
//        Bindings bindings = queries.get(0).getBindingsList().isEmpty() ? List.of()
//                : queries.get(0).getBindingsList().get(0);
//
//        String bindingLog = bindings.entrySet().stream()
//                .map(e -> e.getKey() + ":\"" + e.getValue() + "\"")
//                .collect(Collectors.joining(", ", "[", "]"));
//
//        // 바인딩 적용된 쿼리 생성 (Postgres 스타일 $1, $2 … 치환)
//        String inlinedSql = sql;
//        for (Map.Entry<Integer, Object> entry : bindings.entrySet()) {
//            Object value = entry.getValue();
//            String rendered = renderValue(value);
//            inlinedSql = inlinedSql.replaceFirst("\\$" + (entry.getKey() + 1), rendered);
//        }
//
//        // 최종 로그 출력
//        log.info("Connection Id: {}, Success: {}, Time: {} ms\nQuery: {}\nBindings: {}\n{}",
//                connId,
//                execInfo.isSuccess(),
//                time.toMillis(),
//                sql,
//                bindingLog,
//                inlinedSql
//        );
//    }

    private String renderValue(Object value) {
        if (value == null) return "NULL";
        if (value instanceof Number) return value.toString();
        return "'" + value.toString().replace("'", "''") + "'";
    }
}
