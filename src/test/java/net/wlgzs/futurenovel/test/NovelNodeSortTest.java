package net.wlgzs.futurenovel.test;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import net.wlgzs.futurenovel.bean.NovelNode;
import net.wlgzs.futurenovel.utils.NovelNodeComparator;
import org.junit.Assert;
import org.junit.Test;

public class NovelNodeSortTest {

    @Test
    public void covertNumberTest() {
        Assert.assertEquals(250, NovelNodeComparator.chineseNumber2Int(NovelNodeComparator.getFirstNumberStr("我是贰佰伍拾")));
        List<NovelNode> novelNodes1 = Arrays.asList(
            new TestNovelNode("第一章"),
            new TestNovelNode("序章"),
            new TestNovelNode("第 1 章")
        );
        List<NovelNode> novelNodes2 = Arrays.asList(
            new TestNovelNode("第二章"),
            new TestNovelNode("序章"),
            new TestNovelNode("第一章")
        );
        novelNodes1.sort(NovelNodeComparator::compareByTitle);
        novelNodes2.sort(NovelNodeComparator::compareByTitle);
        Assert.assertEquals("序章,第 1 章,第一章", buildJoinedString(novelNodes1));
        Assert.assertEquals("序章,第一章,第二章", buildJoinedString(novelNodes2));
    }

    private String buildJoinedString(List<NovelNode> list) {
        return list.stream().map(NovelNode::getTitle).collect(Collectors.joining(","));
    }

    @EqualsAndHashCode
    static class TestNovelNode implements NovelNode {

        private final String title;

        TestNovelNode(String title) {
            this.title = title;
        }

        @Override
        public UUID getParentUUID() {
            return null;
        }

        @Override
        public UUID getThisUUID() {
            return null;
        }

        @Override
        public String getTitle() {
            return title;
        }

    }
}
