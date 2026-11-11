
package no.pgr301.exam.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import no.pgr301.exam.model.AnalyzeResponse;
import no.pgr301.exam.model.GiftAnalysis;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class WishlistService {

    private final S3Service s3;
    private final RekognitionService rekognition;
    private final BedrockService bedrock;
    private final Counter giftSuggestionsCounter;

    public WishlistService(S3Service s3,
                           RekognitionService rekognition,
                           BedrockService bedrock,
                           MeterRegistry meterRegistry) {
        this.s3 = s3;
        this.rekognition = rekognition;
        this.bedrock = bedrock;
        this.giftSuggestionsCounter = Counter.builder("wishifai.gift.suggestions")
                .description("Total number of gift suggestions generated")
                .register(meterRegistry);
    }

    public AnalyzeResponse process(byte[] bytes, String filename, String contentType) {
        String s3Key = s3.upload(bytes, filename, contentType);
        List<String> labels = rekognition.detectLabels(bytes);
        GiftAnalysis analysis = bedrock.suggestGifts(labels);

        giftSuggestionsCounter.increment(analysis.suggestions().size());

        return new AnalyzeResponse(
                s3Key,
                labels,
                analysis.roomAnalysis(),
                analysis.recipientProfile(),
                analysis.suggestions(),
                Instant.now().toString()
        );
    }
}
