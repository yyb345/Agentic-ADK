import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.dashscope.embeddings.DashScopeEmbeddings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean(name = "embedding")
    public Embeddings embedding() {
        Embeddings embeddings = new DashScopeEmbeddings();
        return embeddings;
    }
}
