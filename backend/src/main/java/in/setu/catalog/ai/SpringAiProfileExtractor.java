package in.setu.catalog.ai;

import java.util.List;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.parser.BeanOutputParser;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

/** Spring AI adapter. The schema-derived parser constrains the model response to UserProfilePatch. */
@Component
@ConditionalOnBean(ChatClient.class)
public final class SpringAiProfileExtractor implements ProfileExtractionPort {
    private static final String SYSTEM_INSTRUCTIONS = """
        Extract only facts explicitly stated in the citizen's message.
        Do not infer, estimate, or invent omitted facts. Leave an uncertain field null.
        Normalize West Bengal, WB, and Bengal to WEST_BENGAL; student/studying to STUDENT;
        and unemployed/jobless to UNEMPLOYED. Convert Indian income expressions to annual rupees,
        e.g. 2.5 lakh per year is 250000. Do not decide eligibility, recommend a scheme, or return advice.
        """;

    private final ChatClient chatClient;

    public SpringAiProfileExtractor(ChatClient chatClient) { this.chatClient = chatClient; }

    @Override
    public UserProfilePatch extract(String userMessage) {
        if (userMessage == null || userMessage.isBlank()) return new UserProfilePatch(null, null, null, null);
        BeanOutputParser<UserProfilePatch> parser = new BeanOutputParser<>(UserProfilePatch.class);
        Prompt prompt = new Prompt(List.of(
            new SystemMessage(SYSTEM_INSTRUCTIONS),
            new UserMessage(userMessage + "\n\n" + parser.getFormat())
        ));
        return parser.parse(chatClient.call(prompt).getResult().getOutput().getContent());
    }
}
