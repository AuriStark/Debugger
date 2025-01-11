package scraper.nodes.test;

import scraper.annotations.NotNull;
import scraper.annotations.FlowKey;
import scraper.annotations.NodePlugin;
import scraper.api.NodeException;
import scraper.api.FlowMap;
import scraper.api.FunctionalNodeContainer;
import scraper.api.FunctionalNode;
import scraper.api.T;

/**
 * @author Albert Schimpf
 */
@NodePlugin(value = "0.0.1", deprecated = true)
public final class TwoInputs implements FunctionalNode {

    @FlowKey
    private T<String> input1 = new T<>(){};

    @FlowKey
    private T<String> input2 = new T<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull FlowMap o) throws NodeException {
        // do nothing as pure as it gets
    }
}
