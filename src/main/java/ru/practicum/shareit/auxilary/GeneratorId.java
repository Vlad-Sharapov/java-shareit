package ru.practicum.shareit.auxilary;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class GeneratorId {

    private Long id = 0L;

    public Long incrementId() {
        return ++id;
    }
}
