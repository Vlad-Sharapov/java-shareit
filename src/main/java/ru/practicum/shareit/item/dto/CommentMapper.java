package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CommentMapper {

    public static Comment toComment(CommentDto commentDto, User user, Item item) {
        Comment comment = new Comment();
        comment.setId(commentDto.getItemId());
        comment.setText(commentDto.getText());
        comment.setAuthor(user);
        comment.setItem(item);
        return comment;
    }

    public static CommentDto toCommentDto(Comment comment) {

        ZoneId zoneId = ZoneId.of("UTC+3");
        ZonedDateTime zonedStartDate = comment.getCreated().atZone(zoneId);

        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor().getName())
                .itemId(comment.getItem().getId())
                .created(zonedStartDate.toLocalDateTime())
                .build();
    }

    public static List<CommentDto> toCommentDto(Collection<Comment> comments) {
        return comments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }
}
