package com.nowcoder.community.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author aiolia
 * @version 1.0
 * @create 2023/1/29
 */
@Slf4j
@Component
public class SensitiveFilter
{
    //替换符
    private static final String REPLACEMENT = "***";

    //根节点
    private TrieNode rootNode = new TrieNode();

    @PostConstruct
    public void init()
    {
        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        )
        {
            String keyword;
            while ((keyword = reader.readLine()) != null)
            {
                //添加到前缀树
                addKeyWord(keyword);
            }
        } catch (IOException e)
        {
            log.error("加载敏感词文件失败：" + e.getMessage());
        }
    }

    //将一个敏感词添加到前缀树中
    private void addKeyWord(String keyword)
    {
        TrieNode tempNode = rootNode;
        for (int i = 0; i < keyword.length(); i++)
        {
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);
            if (subNode == null)
            {
                //初始化子节点
                subNode = new TrieNode();
                tempNode.addSubNodes(c, subNode);
            }
            //指向子节点，进入下一轮循环
            tempNode = subNode;
            //设置结束标识
            if (i == keyword.length() - 1)
            {
                tempNode.setKeywordEnd(true);
            }
        }
    }

    /**
     * 过滤敏感词
     *
     * @param text 待过滤的文本
     * @return 过滤后的文本
     */
    public String filter(String text)
    {
        if (StringUtils.isBlank(text))
        {
            return null;
        }
        //指针1
        TrieNode tempNode = rootNode;
        //指针2
        int begin = 0;
        //指针3
        int position = 0;
        //结果
        StringBuilder sb = new StringBuilder();

        while (position < text.length())
        {
            char c = text.charAt(position);
            //跳过符号
            if (isSymbol(c))
            {
                if (tempNode == rootNode)
                {
                    sb.append(c);
                    begin++;
                }
                position++;
                continue;
            }
            //检查下级节点
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null)
            {
                //以begin开头的字符串不是敏感词
                sb.append(text.charAt(begin));
                position = ++begin;
                tempNode = rootNode;
            } else if (tempNode.isKeywordEnd())
            {
                //发现敏感词
                sb.append(REPLACEMENT);
                begin = ++position;
                tempNode = rootNode;
            } else
            {
                position++;

            }
            // 提前判断postion是不是到达结尾，要跳出while,如果是，则说明begin-position这个区间不是敏感词，但是里面不一定没有
            if (position == text.length() && begin != position)
            {
                // 说明还剩下一段需要判断，则把position==++begin
                // 并且当前的区间的开头字符是合法的
                sb.append(text.charAt(begin));
                position = ++begin;
                tempNode = rootNode;  // 前缀表从头开始了
            }
        }
        return sb.toString();
    }

    //判断是否为符号
    private boolean isSymbol(Character c)
    {
        //0x2E80~0x9FFF是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    //前缀树节点
    private class TrieNode
    {
        //关键词结束标识
        private boolean isKeywordEnd = false;

        //子节点(key是下级字符，value是下级节点)
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd()
        {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd)
        {
            isKeywordEnd = keywordEnd;
        }

        //添加子节点
        public void addSubNodes(Character c, TrieNode node)
        {
            subNodes.put(c, node);
        }

        //获取子节点
        public TrieNode getSubNode(Character c)
        {
            return subNodes.get(c);
        }
    }
}
