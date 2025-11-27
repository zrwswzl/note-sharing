package com.project.login.service.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionBoostMode;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreMode;
import com.project.login.convert.SearchConvert;
import com.project.login.model.dto.search.NoteSearchDTO;
import com.project.login.model.vo.NoteSearchVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final ElasticsearchClient esClient;

    @Qualifier("searchConvert")
    private final SearchConvert convert;

    public List<NoteSearchVO> searchNotes(NoteSearchDTO dto) {
        String keyword = dto.getKeyword();

        try {
            var response = esClient.search(s -> s
                            .index("notes")
                            .size(30)
                            .query(q -> q
                                    .functionScore(fs -> fs
                                            .query(base -> base
                                                    .multiMatch(m -> m
                                                            .query(keyword)
                                                            // ✅ 修正1：content_summary -> contentSummary
                                                            .fields("title^2", "contentSummary") 
                                                    )
                                            )
                                            .boostMode(FunctionBoostMode.Sum)
                                            .scoreMode(FunctionScoreMode.Sum)
                                            .functions(fns -> fns
                                                    .scriptScore(sc -> sc
                                                            .script(scr -> scr
                                                                    .source("""
                                                                        // ========== 综合评分公式 ==========
                                                                        
                                                                        // ✅ 修正2：更安全的空值检查 (doc['field'] 不会是 null，要检查 size())
                                                                        double safeLong(def docs, String fieldName) {
                                                                            if (!docs.containsKey(fieldName) || docs[fieldName].size() == 0) return 0;
                                                                            return docs[fieldName].value;
                                                                        }

                                                                        double baseScore = _score * 0.5;

                                                                        // ✅ 修正3：使用正确的字段名 viewCount, likeCount
                                                                        double views = safeLong(doc, 'viewCount');
                                                                        double likes = safeLong(doc, 'likeCount');
                                                                        
                                                                        double viewsScore = Math.log(views + 1) * 0.2;
                                                                        double likesScore = Math.log(likes + 1) * 0.2;

                                                                        long updated = 0L;
                                                                        // 检查 updatedAt 是否存在
                                                                        if (doc.containsKey('updatedAt') && doc['updatedAt'].size() > 0) {
                                                                            // ✅ JavaTimeModule 修复后，这里通常可以直接取 value 
                                                                            // 如果是较新版本 ES，日期字段 value 是 ZonedDateTime，可以直接 .toInstant().toEpochMilli()
                                                                            // 如果报错，可以尝试 doc['updatedAt'].value.toInstant().toEpochMilli()
                                                                            // 或者最稳妥的写法（针对 ES 8.x）：
                                                                            updated = doc['updatedAt'].value.toInstant().toEpochMilli();
                                                                        }

                                                                        long now = System.currentTimeMillis();
                                                                        // 防止时间倒流导致的负数
                                                                        double days = Math.max(0, (now - updated) / 86400000.0);
                                                                        double recency = 1.0 / (days + 1.0);
                                                                        double recencyScore = recency * 0.1;

                                                                        return baseScore + viewsScore + likesScore + recencyScore;
                                                                    """)
                                                                    .lang("painless")
                                                            )
                                                    )
                                            )
                                    )
                            ),
                    Object.class
            );

            return response.hits()
                    .hits()
                    .stream()
                    .map(hit -> convert.toSearchVO(hit.source()))
                    .collect(Collectors.toList());

        } catch (IOException e) {
            throw new RuntimeException("搜索失败", e);
        }
    }
}
