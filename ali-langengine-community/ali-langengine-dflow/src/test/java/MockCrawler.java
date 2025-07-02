import com.alibaba.dflow.DFlow;
import com.alibaba.dflow.internal.DFlowConstructionException;
import com.alibaba.langengine.core.dflow.agent.tool.DFlowTool;

public class MockCrawler {
    public DFlow<String> run(String url) throws DFlowConstructionException {
        return DFlow.just("### 相关URL： 以下是一些与当前任务相关的产品链接（京东商品页面）： 1. [ROG幻X 2025 锐龙AI MAX 游戏本](//item.jd.com/100177226228.html) 2. [逆昂128g内存酷睿i7升24核电竞台式机](//item.jd.com/10096148836920.html) 3. [夏惠128G内存酷睿i9升24核2T固态RTX4060独显台式电脑主机](//item.jd.com/10103845559644.html) 4. [微科达128g大内存英特尔14代酷睿i7台式机电脑主机](//item.jd.com/10132837410656.html) 5. [英邦达英特尔酷睿i7升十八核4060独显64G内存组装台式机电脑主机](//item.jd.com/10074061343671.html) 6. [daiteng128g大内存英特尔14代酷睿i7台式机电脑主机](//item.jd.com/10134300277729.html) 7. [联想小新Pro14 AI超能本](//item.jd.com/100109152429.html) 8. [速度玩家14代英特尔酷睿i7台式机电脑主机](//item.jd.com/10081541028224.html) 9. [英迪达英特尔酷睿i7升十八核4060独显64g内存电脑台式机主机](//item.jd.com/10111055810908.html) 10. [英邦达英特尔酷睿i7升十八核4060独显64G内存组装台式机电脑主机](//item.jd.com/10086103952412.html) 11. [速度玩家14代英特尔酷睿i7台式机电脑主机](//item.jd.com/10081541028225.html) 12. [领睿英特尔酷睿i7升24核RTX4060独显64G内存台式电脑主机](//item.jd.com/10076027856176.html) 13. [飞利浦 31.5英寸 2K 144Hz 曲屏显示器](//item.jd.com/100035177839.html) 14. [lgbm英特尔酷睿i7/i9级电脑台式机主机40/5060独显64g内存](//item.jd.com/10106000712238.html) 15. [LAVRIKOW2025拯救系列新款笔记本电脑](//item.jd.com/10119659685281.html) 16. [gpam128g内存酷睿i9升24核电竞台式机电脑主机](//item.jd.com/10126005973813.html) 17. [卫战神128g【英特尔14代酷睿i7】24核4060独显电竞台式机电脑主机](//item.jd.com/10112134438128.html) 18. [深科显128g内存酷睿i7升48芯台式机电脑主机](//item.jd.com/10121852561707.html) 19. [华硕ProArt创16 AMD锐龙AI 9 HX370 RTX4060游戏设计AI电脑](//item.jd.com/100125527306.html) 20. [技械骑士128g内存酷睿i7升24核台式机电脑](//item.jd.com/10113779986493.html) 21. [gpam128g内存酷睿i9升24核电竞台式机电脑主机](//item.jd.com/");
    }
}
