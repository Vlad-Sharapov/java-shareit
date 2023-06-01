package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

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

    public static List<CommentDto> toCommentDto(Iterable<Comment> comments) {

        List<CommentDto> commentsDto = new ArrayList<>();

        for (Comment comment : comments) {
            commentsDto.add(toCommentDto(comment));
        }
        return commentsDto;
    }
}
