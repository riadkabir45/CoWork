package com.aritra.d.riad.CoWork.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aritra.d.riad.CoWork.dto.TaskCommentDTO;
import com.aritra.d.riad.CoWork.model.TaskComment;
import com.aritra.d.riad.CoWork.model.TaskInstances;
import com.aritra.d.riad.CoWork.model.TaskReaction;
import com.aritra.d.riad.CoWork.model.Users;
import com.aritra.d.riad.CoWork.repository.TaskCommentRepository;
import com.aritra.d.riad.CoWork.repository.TaskInstancesRepository;
import com.aritra.d.riad.CoWork.repository.TaskReactionRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class TaskCommentService {

    @Autowired
    private TaskCommentRepository taskCommentRepository;

    @Autowired
    private TaskInstancesRepository taskInstancesRepository;

    @Autowired
    private TaskReactionRepository taskReactionRepository;

    @Autowired
    private UserService userService;

    public TaskCommentDTO createComment(String taskInstanceId, String content, Users author, String parentCommentId) {
        TaskInstances taskInstance = taskInstancesRepository.findById(taskInstanceId)
                .orElseThrow(() -> new RuntimeException("Task instance not found"));

        TaskComment comment = new TaskComment();
        comment.setContent(content);
        comment.setTaskInstance(taskInstance);
        comment.setAuthor(author);

        // Handle parent comment for replies
        if (parentCommentId != null && !parentCommentId.isEmpty()) {
            TaskComment parentComment = taskCommentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new RuntimeException("Parent comment not found"));
            comment.setParentComment(parentComment);
        }

        TaskComment savedComment = taskCommentRepository.save(comment);
        log.info("Created comment {} for task instance {}", savedComment.getId(), taskInstanceId);

        return convertToDTO(savedComment, author);
    }

    public TaskCommentDTO updateComment(String commentId, String newContent, Users currentUser) {
        TaskComment comment = taskCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        // Check if the current user is the author
        if (!comment.getAuthor().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only edit your own comments");
        }

        comment.setContent(newContent);
        TaskComment updatedComment = taskCommentRepository.save(comment);
        log.info("Updated comment {}", commentId);

        return convertToDTO(updatedComment, currentUser);
    }

    public void deleteComment(String commentId, Users currentUser) {
        TaskComment comment = taskCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        // Check if the current user is the author
        if (!comment.getAuthor().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only delete your own comments");
        }

        taskCommentRepository.delete(comment);
        log.info("Deleted comment {}", commentId);
    }

    public List<TaskCommentDTO> getCommentsForTaskInstance(String taskInstanceId, Users currentUser) {
        TaskInstances taskInstance = taskInstancesRepository.findById(taskInstanceId)
                .orElseThrow(() -> new RuntimeException("Task instance not found"));

        List<TaskComment> topLevelComments = taskCommentRepository
                .findByTaskInstanceAndParentCommentIsNullOrderByCreatedAtDesc(taskInstance);

        return topLevelComments.stream()
                .map(comment -> convertToDTO(comment, currentUser))
                .collect(Collectors.toList());
    }

    public Page<TaskCommentDTO> getCommentsForTaskInstanceWithPagination(String taskInstanceId, Users currentUser, 
                                                                         int page, int size) {
        TaskInstances taskInstance = taskInstancesRepository.findById(taskInstanceId)
                .orElseThrow(() -> new RuntimeException("Task instance not found"));

        Pageable pageable = PageRequest.of(page, size);
        Page<TaskComment> commentPage = taskCommentRepository
                .findByTaskInstanceAndParentCommentIsNullOrderByCreatedAtDesc(taskInstance, pageable);

        return commentPage.map(comment -> convertToDTO(comment, currentUser));
    }

    public List<TaskCommentDTO> getRepliesForComment(String parentCommentId, Users currentUser) {
        TaskComment parentComment = taskCommentRepository.findById(parentCommentId)
                .orElseThrow(() -> new RuntimeException("Parent comment not found"));

        List<TaskComment> replies = taskCommentRepository.findByParentCommentOrderByCreatedAtAsc(parentComment);

        return replies.stream()
                .map(reply -> convertToDTO(reply, currentUser))
                .collect(Collectors.toList());
    }

    public TaskCommentDTO getCommentById(String commentId, Users currentUser) {
        TaskComment comment = taskCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        return convertToDTO(comment, currentUser);
    }

    // New methods to get comments by task (across all instances)
    public List<TaskCommentDTO> getCommentsForTask(String taskId, Users currentUser) {
        List<TaskComment> topLevelComments = taskCommentRepository
                .findByTaskIdAndParentCommentIsNullOrderByCreatedAtDesc(taskId);

        return topLevelComments.stream()
                .map(comment -> convertToDTO(comment, currentUser))
                .collect(Collectors.toList());
    }

    public Page<TaskCommentDTO> getCommentsForTaskWithPagination(String taskId, Users currentUser, 
                                                               int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TaskComment> commentPage = taskCommentRepository
                .findByTaskIdAndParentCommentIsNullOrderByCreatedAtDesc(taskId, pageable);

        return commentPage.map(comment -> convertToDTO(comment, currentUser));
    }

    public long getCommentCountForTask(String taskId) {
        return taskCommentRepository.countByTaskId(taskId);
    }

    public long getTopLevelCommentCountForTask(String taskId) {
        return taskCommentRepository.countByTaskIdAndParentCommentIsNull(taskId);
    }

    public long getCommentCountForTaskInstance(String taskInstanceId) {
        TaskInstances taskInstance = taskInstancesRepository.findById(taskInstanceId)
                .orElseThrow(() -> new RuntimeException("Task instance not found"));

        return taskCommentRepository.countByTaskInstance(taskInstance);
    }

    public long getTopLevelCommentCountForTaskInstance(String taskInstanceId) {
        TaskInstances taskInstance = taskInstancesRepository.findById(taskInstanceId)
                .orElseThrow(() -> new RuntimeException("Task instance not found"));

        return taskCommentRepository.countByTaskInstanceAndParentCommentIsNull(taskInstance);
    }

    private TaskCommentDTO convertToDTO(TaskComment comment, Users currentUser) {
        TaskCommentDTO dto = new TaskCommentDTO();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setTaskInstanceId(comment.getTaskInstance().getId());
        dto.setAuthor(userService.generateSimpleUserDTO(comment.getAuthor()));
        dto.setParentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null);
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());
        dto.setEdited(comment.isEdited());
        dto.setLikeCount(comment.getLikeCount());
        dto.setDislikeCount(comment.getDislikeCount());
        dto.setReplyCount(comment.getReplies().size());

        // Get user's reaction to this comment
        if (currentUser != null) {
            Optional<TaskReaction> userReaction = taskReactionRepository
                    .findByUserAndReactionTargetTypeAndTargetId(currentUser, 
                            TaskReaction.ReactionTargetType.COMMENT, comment.getId());
            dto.setUserReaction(userReaction.map(r -> r.getReactionType().toString()).orElse(null));
        }

        // Load replies if this is a top-level comment
        if (comment.getParentComment() == null) {
            List<TaskCommentDTO> replies = comment.getReplies().stream()
                    .map(reply -> convertToDTO(reply, currentUser))
                    .collect(Collectors.toList());
            dto.setReplies(replies);
        }

        return dto;
    }
}
