package com.project.login.service.history;

import com.project.login.convert.HistoryConvert;
import com.project.login.mapper.NoteMapper;
import com.project.login.model.dataobject.NoteDO;
import com.project.login.model.dataobject.RemarkDO;
import com.project.login.model.vo.HistoryNoteVO;
import com.project.login.model.vo.HistoryRemarkVO;
import com.project.login.model.vo.RemarkVO;
import com.project.login.repository.RemarkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HistoryService {
    private final RedisTemplate<String,Object> redisTemplate;
    private final RemarkRepository remarkRepository;
    private final HistoryConvert historyConvert;
    private final NoteMapper noteMapper;
    private final String noteHistoryKey="history.note.userId:";
    private final String remarkHistoryKey="history.remark.userId:";
    public void logUserHistoryOfNote(Long loginUserId,Long noteId){
        redisTemplate.opsForZSet().add(noteHistoryKey+loginUserId,noteId, System.currentTimeMillis());
    }
    public void logUserHistoryOfRemark(Long loginUserId,String remarkId){
        redisTemplate.opsForZSet().add(remarkHistoryKey+loginUserId,remarkId,System.currentTimeMillis());

    }
    public Page<HistoryRemarkVO> getRemarkHistory(Long loginUserId,int pageNum,int pageSize){
        long start=(long)(pageNum-1)*pageSize;
        long end=start+pageSize-1;
        Long total=redisTemplate.opsForZSet().zCard(remarkHistoryKey+loginUserId);
        if(total==null||total==0){
            return null;
        }
        List<String> raw=redisTemplate.opsForZSet().reverseRange(remarkHistoryKey+loginUserId,start,end).stream().map(Object::toString).toList();
        if(raw.isEmpty()){
            return null;
        }
        List<HistoryRemarkVO> result=new ArrayList<>();
        raw.forEach(remarkId->{
            Optional<RemarkDO> remarkDOOptional=remarkRepository.findById(remarkId);
            remarkDOOptional.ifPresent(remarkDO -> result.add(historyConvert.toHistoryRemarkVO(remarkDO)));
        });
        Pageable pageable= PageRequest.of(pageNum-1,pageSize);
        return new PageImpl<>(result, pageable,total);
    }
    public Page<HistoryNoteVO> getNoteHistory(Long loginUserId, int pageNum, int pageSize){
        long start=(long)(pageNum-1)*pageSize;
        long end=start+pageSize-1;
        Long total=redisTemplate.opsForZSet().zCard(noteHistoryKey+loginUserId);
        if(total==null||total==0){
            return null;
        }
        List<Long> raw=redisTemplate.opsForZSet().reverseRange(noteHistoryKey+loginUserId,start,end).stream().map(obj->Long.parseLong(obj.toString())).toList();
        if(raw.isEmpty()){
            return null;
        }
        List<HistoryNoteVO> result=new ArrayList<>();
        raw.forEach(noteId->{
            result.add(historyConvert.toHistoryNoteVO(noteMapper.selectById(noteId)));

        });
        Pageable pageable= PageRequest.of(pageNum-1,pageSize);
        return new PageImpl<>(result, pageable,total);
    }
    public void cleanOldHistory() {
        // 30天前的时间戳
        long cutoff = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000;

        // 前缀数组，依次处理 note 和 remark 历史
        String[] prefixes = {noteHistoryKey, remarkHistoryKey};

        for (String prefix : prefixes) {
            // 使用 Redis SCAN 迭代 Key（非阻塞）
            var connection = redisTemplate.getConnectionFactory().getConnection();
            var scanOptions = org.springframework.data.redis.core.ScanOptions.scanOptions()
                    .match(prefix + "*")
                    .count(100) // 每次扫描 100 个 Key，可根据压力调整
                    .build();

            try (var cursor = connection.scan(scanOptions)) {
                while (cursor.hasNext()) {
                    String key = new String(cursor.next());
                    // 清理 ZSet 中 score <= cutoff 的记录
                    redisTemplate.opsForZSet().removeRangeByScore(key, 0, cutoff);
                }
            } catch (Exception e) {
                log.error("[HistoryCleaner] 清理历史记录失败", e);
            }
        }

        log.info("[HistoryCleaner] 已清理过期历史记录");
    }
}
