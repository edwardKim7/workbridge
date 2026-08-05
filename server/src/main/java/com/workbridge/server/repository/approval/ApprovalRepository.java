package com.workbridge.server.repository.approval;
import com.workbridge.server.domain.approval.*;
import com.workbridge.server.dto.approval.ApproverOptionResponse;
import com.workbridge.server.mapper.approval.ApprovalMapper;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.*;
@Repository public class ApprovalRepository {
    private final ApprovalMapper mapper;
    public ApprovalRepository(ApprovalMapper mapper) {
        this.mapper = mapper;
    }
    public List<ApprovalRecord>findPage(String scope, Long actorId, ApprovalStatus status, ApprovalType type, String keyword,
        int offset, int size) {
        return mapper.selectPage(scope, actorId, status, type, keyword, offset, size);
    }
    public long count(String scope, Long actorId, ApprovalStatus status, ApprovalType type, String keyword) {
        return mapper.countPage(scope, actorId, status, type, keyword);
    }
    public Optional<ApprovalRecord>findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id));
    }
    public List<ApprovalHistoryRecord>findHistories(Long id) {
        return mapper.selectHistories(id);
    }
    public List<ApproverOptionResponse>findActiveApprovers() {
        return mapper.selectActiveApprovers();
    }
    public boolean isActiveApprover(Long id) {
        return mapper.countActiveApprover(id)>0;
    }
    public Long insert(ApprovalRecord r, Long actor) {
        mapper.insert(r, actor);
        return r.getApprovalId();
    }
    public boolean updateDraft(ApprovalUpdateCommand c) {
        return mapper.updateDraft(c) == 1;
    }
    public boolean transitionDraft(Long id, Long requester, ApprovalStatus target, Long actor, long version, LocalDateTime now) {
        return mapper.transitionDraft(id, requester, target, actor, version, now) == 1;
    }
    public boolean process(Long id, Long approver, ApprovalStatus target, Long actor, long version, LocalDateTime now) {
        return mapper.processRequested(id, approver, target, actor, version, now) == 1;
    }
    public void history(Long id, ApprovalStatus from, ApprovalStatus to, ApprovalAction action, Long actor, String opinion,
        String trace, LocalDateTime at) {
        mapper.insertHistory(id, from, to, action, actor, opinion, trace, at);
    }
    public List<ApprovalRecord>findRecent(int limit) {
        return mapper.selectRecent(limit);
    }
}
