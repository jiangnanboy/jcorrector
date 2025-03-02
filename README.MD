# jcorrector

中文文本纠错工具。音似、形似错字（或变体字）纠正，可用于中文拼音、笔画输入法的错误纠正。项目为java开发。

**jcorrector**

1.利用n-gram语言模型检测错别字位置，通过拼音音似特征、笔画五笔编辑距离特征及语言模型句子概率值特征纠正错别字。

2.利用深度学习模型（如macbert等）进行中文end2end拼写纠错。


**Guide**

- [Question](#Question)
- [Solution](#Solution)
- [Feature](#Feature)
- [Usage](#usage)
- [Test](#Test)
- [Dataset](#Dataset)
- [Neural-Net](#Neural-Net)
- [Todo](#Todo)
- [QQ](#QQ)
- [Cite](#Cite)
- [License](#License)
- [Reference](#reference)

## Question

中文文本纠错任务，常见错误类型包括：

- 谐音字词，如 配副眼睛-配副眼镜
- 混淆音字词，如 流浪织女-牛郎织女
- 字词顺序颠倒，如 伍迪艾伦-艾伦伍迪
- 字词补全，如 爱有天意-假如爱有天意
- 形似字错误，如 高梁-高粱
- 中文拼音全拼，如 xingfu-幸福
- 中文拼音缩写，如 sz-深圳
- 语法错误，如 想象难以-难以想象

当然，针对不同业务场景，这些问题并不一定全部存在，比如输入法中需要处理前四种，搜索引擎需要处理所有类型，语音识别后文本纠错只需要处理前两种，
其中'形似字错误'主要针对五笔或者笔画手写输入等。本项目重点解决其中的谐音、混淆音、形似字错误、中文拼音全拼、语法错误带来的纠错任务。


## Solution
### 规则的解决思路
1. 中文纠错分为两步走，第一步是错误检测，第二步是错误纠正；
2. 错误检测部分先通过hanlp分词器切词，由于句子中含有错别字，所以切词结果往往会有切分错误的情况，这样从字粒度和词粒度两方面检测错误，
整合这两种粒度的疑似错误结果，形成疑似错误位置候选集；
3. 错误纠正部分，是遍历所有的疑似错误位置，并使用音似、形似词典替换错误位置的词，然后通过语言模型计算句子概率值，对所有候选集结果比较并排序，得到最优纠正词。


## Feature
### 模型
* berkeleylm：berkeleylm统计语言模型工具，规则方法，语言模型纠错，利用混淆集，扩展性强

### 错误检测
* 字粒度：语言模型句子概率值检测某字的似然概率值低于句子文本平均值，则判定该字是疑似错别字的概率大。
* 词粒度：切词后不在词典中的词是疑似错词的概率大。

### 错误纠正
* 通过错误检测定位所有疑似错误后，取所有疑似错字的音似、形似候选词，
* 使用候选词替换，基于语言模型得到类似翻译模型的候选排序结果，得到最优纠正词。

#### ngram模型
* berkeleylm源码已集成到项目中

## Usage
见【examples/ngram】

模型下载

<!DOCTYPE html>
<html>
<head>
</head>
<body>
<table style="width: 80%;">
  <tr>
      <td style="width: 20%;"><div align="center"><strong>模型</strong></div></td>
      <td style="width: 30%;"><div align="center"><strong>HuggingFace</strong></div></td>
  </tr>
  
  <tr>
      <td><center>jcorrector_ngram</center></td>
      <td><center>🤗<a href="https://huggingface.co/jiangnanboy/jcorrector_ngram">jcorrector_ngram</a></center></td>
  </tr>

</table>
</body>
</html>

### 文本纠错

``` java
import sy.core.spelling.Corrector;

Corrector corrector = new Corrector();
String result = corrector.correct("少先队员因该为老人让坐");
System.out.println(result);
```

output:
```
[{"endIdx":6,"correct":"应该","startIdx":4,"type":"拼写错误","error":"因该"},{"endIdx":11,"correct":"让座","startIdx":9,"type":"拼写错误","error":"让坐"}]
```

### 错误检测

``` java
import sy.core.spelling.Detector;

Detector detector = new Detector();
String result = detector.detect(sentence);
System.out.println(result);
```

output:
```
[[因该, 4, 6, confusion], [让坐, 9, 11, confusion]]
```

默认字粒度、词粒度的纠错都打开，可以通过enableCharError()和enableWordError()进行设置。

### 加载自定义混淆集

通过加载自定义混淆集，支持用户纠正已知的错误

示例[LoadDetectorDict.setCustomConfusionDict("/path")](sy/spelling/LoadDetectorDict.java)

### 成语、专名纠错

见【examples/proper】

``` java
List<String> testLine = List.of(
                "报应接中迩来",
                "这块名表带带相传",
                "这块名表代代相传",
                "他贰话不说把牛奶喝完了",
                "这场比赛我甘败下风",
                "这场比赛我甘拜下封",
                "这家伙还蛮格尽职守的",
                "报应接中迩来",  // 接踵而来
                "人群穿流不息",
                "这个消息不径而走",
                "这个消息不胫儿走",
                "眼前的场景美仑美幻简直超出了人类的想象",
                "看着这两个人谈笑风声我心理不由有些忌妒",
                "有了这一番旁证博引",
                "有了这一番旁针博引",
                "这群鸟儿迁洗到远方去了",
                "这群鸟儿千禧到远方去了",
                "美国前总统特琅普给普京点了一个赞，特朗普称普金做了一个果断的决定"
        );
        for(String line : testLine) {
            System.out.println(properCorrector.correct(line));
        }
```

output:
```
[{"endIdx":6,"correct":"接踵而来","startIdx":2,"type":"专名错误","error":"接中迩来"}]
[{"endIdx":8,"correct":"代代相传","startIdx":4,"type":"专名错误","error":"带带相传"}]
[]
[{"endIdx":5,"correct":"二话不说","startIdx":1,"type":"专名错误","error":"贰话不说"}]
[{"endIdx":9,"correct":"甘拜下风","startIdx":5,"type":"专名错误","error":"甘败下风"}]
[{"endIdx":9,"correct":"甘拜下风","startIdx":5,"type":"专名错误","error":"甘拜下封"}]
[{"endIdx":9,"correct":"恪尽职守","startIdx":5,"type":"专名错误","error":"格尽职守"}]
[{"endIdx":6,"correct":"接踵而来","startIdx":2,"type":"专名错误","error":"接中迩来"}]
[{"endIdx":6,"correct":"川流不息","startIdx":2,"type":"专名错误","error":"穿流不息"}]
[{"endIdx":8,"correct":"不胫而走","startIdx":4,"type":"专名错误","error":"不径而走"}]
[{"endIdx":8,"correct":"不胫而走","startIdx":4,"type":"专名错误","error":"不胫儿走"}]
[{"endIdx":9,"correct":"美轮美奂","startIdx":5,"type":"专名错误","error":"美仑美幻"}]
[{"endIdx":10,"correct":"谈笑风生","startIdx":6,"type":"专名错误","error":"谈笑风声"}]
[{"endIdx":9,"correct":"旁征博引","startIdx":5,"type":"专名错误","error":"旁证博引"}]
[{"endIdx":9,"correct":"旁征博引","startIdx":5,"type":"专名错误","error":"旁针博引"}]
[{"endIdx":6,"correct":"迁徙","startIdx":4,"type":"专名错误","error":"迁洗"}]
[{"endIdx":6,"correct":"迁徙","startIdx":4,"type":"专名错误","error":"千禧"}]
[{"endIdx":8,"correct":"特朗普","startIdx":5,"type":"专名错误","error":"特琅普"},{"endIdx":23,"correct":"普京","startIdx":21,"type":"专名错误","error":"普金"}]
```
### 基于模板中文语法纠错

见【examples/gec】

``` java
String templatePath = GecCheck.class.getClassLoader().getResource(PropertiesReader.get("template")).getPath().replaceFirst("/", "");
GecCheck gecRun = new GecCheck();
gecRun.init(templatePath);
String sentence;
while (true) {
    System.out.println("Please input a sentence:");
    Scanner scanner = new Scanner(System.in);
    sentence = scanner.next();
    String infoStr = gecRun.checkCorrect(sentence);
    if(StringUtils.isNotBlank(infoStr)) {
        System.out.println(infoStr);
    }
}
```

output:
```
爸爸看完小品后忍俊不禁笑了起来。
爸爸看完小品后忍俊不禁。
[{"correct":"忍俊不禁","start":7,"end":15,"error":"忍俊不禁笑了起来"}]

孙中山辞职后不再过问政治，决心尽瘁社会上事业，开始着手社会革命。
孙中山辞职后不再过问政治，决心尽瘁社会上事业，开始社会革命。
[{"correct":"开始","start":23,"end":27,"error":"开始着手"}]

在俄国社会民主工党第二次代表大会中，列宁提出效仿民意党，建立一套围绕少数“职业革命家”为核心、党员对核心高度服从的集权化的组织模式，即民主集中制。
在俄国社会民主工党第二次代表大会中，列宁提出效仿民意党，建立一套少数“职业革命家”为核心、党员对核心高度服从的集权化的组织模式，即民主集中制。
[{"correct":"少数“职业革命家”为核心","start":32,"end":46,"error":"围绕少数“职业革命家”为核心"}]
```

#### 训练

ngram语言模型的训练及加载见sy/core/ngram/NGramModel

## Test
以下是部分测试结果：

### detector:
```
少先队员因该为老人让坐
[[因该, 4, 6, confusion], [让坐, 9, 11, confusion]]

机七学习是人工智能领遇最能体现智能的一个分知
[[机, 0, 1, char], [领, 9, 10, char], [遇, 10, 11, char], [分, 20, 21, char], [知, 21, 22, char]]

一只小鱼船浮在平净的河面上
[[平净, 7, 9, word]]

我的家乡是有明的渔米之乡
[[渔米之乡, 8, 12, confusion]]
```

### corrector:
```
少先队员因该为老人让坐
[{"endIdx":6,"correct":"应该","startIdx":4,"type":"拼写错误","error":"因该"},{"endIdx":11,"correct":"让座","startIdx":9,"type":"拼写错误","error":"让坐"}]

真麻烦你了。希望你们好好的跳无
[{"endIdx":14,"correct":["条","桃","跳","调","挑","逃","眺","兆","窕","佻","笤"],"startIdx":13,"type":"拼写错误","error":"跳"},{"endIdx":15,"correct":["舞","物","污","武","务","屋","无","乌","于","恶","伍","误","午","雾","吴","悟","亡","勿","巫","五","坞","梧","芜","捂","侮","呜","诬","晤","蜈","鹉"],"startIdx":14,"type":"拼写错误","error":"无"}]

机七学习是人工智能领遇最能体现智能的一个分知
[{"endIdx":1,"correct":["其","继","既","冀","吉","奇","鸡","急","几","给","即","基","骑","祭","挤","借","激","寄","吃","疾","际","击","积","棘","己","忌","籍","寂","集","系","机","计","济","剂","季","迹","稽","脊","记","辑","荠","肌","极","饥","级","及","箕","期","居","嫉","畸","绩","讥","叽","唧","鲫","技","妓","圾","纪"],"startIdx":0,"type":"拼写错误","error":"机"},{"endIdx":11,"correct":["域","语","与","于","羽","愈","郁","育","偶","浴","淤","喻","芋","雨","尉","宇","愉","愚","吁","遇","狱","逾","欲","寓","隅","榆","裕","娱","御","鱼","余","藕","豫","迂","誉","玉","屿","蔚","予","谷","渔","预","舆"],"startIdx":10,"type":"拼写错误","error":"遇"},{"endIdx":21,"correct":["粪","纷","芬","愤","坟","粉","分","奋","氛","焚","忿","吩","份"],"startIdx":20,"type":"拼写错误","error":"分"},{"endIdx":22,"correct":["支","值","指","质","枝","芝","氏","旨","殖","治","织","汁","肢","植","掷","制","址","挚","直","至","只","知","滞","侄","秩","致","趾","脂","置","吱","帜","智","纸","职","识","窒","执","志","稚","征","之","止","蜘"],"startIdx":21,"type":"拼写错误","error":"知"}]

一只小鱼船浮在平净的河面上
[{"endIdx":9,"correct":["平静","平经","冯净","屏净","坪净","评净","平精","凭净","平净","萍净","瓶净","平京","乒净","苹净","平景","平晶","平靖","平井","平挣","平争","平峥","平铮","平劲","平径","平惊","平鲸","平睁","平镜","平颈","平敬","平茎","平睛","平诤","平警","平兢","平荆","平筝","平境","平竟","平狰","平阱","平更","平竞"],"startIdx":7,"type":"拼写错误","error":"平净"}]

我的家乡是有明的渔米之乡
[{"endIdx":12,"correct":"鱼米之乡","startIdx":8,"type":"拼写错误","error":"渔米之乡"}]

遇到逆竟时，我们必须勇于面对，而且要愈挫愈勇，这样我们才能朝著成功之路前进。
[{"endIdx":31,"correct":"朝着","startIdx":29,"type":"拼写错误","error":"朝著"}]

人生就是如此，经过磨练才能让自己更加拙壮，才能使自己更加乐观。
[{"endIdx":11,"correct":"磨炼","startIdx":9,"type":"拼写错误","error":"磨练"},{"endIdx":20,"correct":["茁壮","拙装","拙庄","旺壮","卓壮","捉壮","出壮","着壮","咄壮","屈壮","汪壮","酌壮","浊壮","拙状","桌壮","拙壮","拙桩","啄壮","琢壮","灼壮","拙妆","倔壮","掘壮","拙撞"],"startIdx":18,"type":"拼写错误","error":"拙壮"}]
```

## Dataset

数据集来自人民日报2014版，将每句进行分字处理。

## Neural-Net
利用深度学习进行端到端中文拼写纠错。

   ### Macbert
    
    这里利用java加载macbert模型，并进行中文拼写纠错，具体见【https://github.com/jiangnanboy/macbert-java-onnx】
    
   ##### usage
   
   模型下载
<!DOCTYPE html>
<html>
<head>
</head>
<body>
<table style="width: 80%;">
  <tr>
      <td style="width: 20%;"><div align="center"><strong>模型</strong></div></td>
      <td style="width: 30%;"><div align="center"><strong>HuggingFace</strong></div></td>
  </tr>
  
  <tr>
      <td><center>jcorrector_macbert_v1</center></td>
      <td><center>🤗<a href="https://huggingface.co/jiangnanboy/jcorrector_macbert_v1">jcorrector_macbert_v1</a></center></td>
  </tr>

  <tr>
      <td><center>jcorrector_macbert_v2</center></td>
      <td><center>🤗<a href="https://huggingface.co/jiangnanboy/jcorrector_macbert_v2">jcorrector_macbert_v2</a></center></td>
  </tr>

<tr>
      <td><center>jcorrector_macbert_v3</center></td>
      <td><center>🤗<a href="https://huggingface.co/jiangnanboy/jcorrector_macbert_v3">jcorrector_macbert_v3</a></center></td>
  </tr>

</table>
</body>
</html>
   
   1.examples/dl/MacBertCorrect
    
   ```
    String text = "今天新情很好。";
    Pair<BertTokenizer, Map<String, OnnxTensor>> pair = null;
    try {
        pair = parseInputText(text);
    } catch (Exception e) {
        e.printStackTrace();
    }
    var predString = predCSC(pair);
    List<Pair<String, String>> resultList = getErrors(predString, text);
    for(Pair<String, String> result : resultList) {
        System.out.println(text + " => " + result.getLeft() + " " + result.getRight());
    }
   ```
    
   2.output
   
   ```
    String text = "今天新情很好。";
    
    tokens -> [[CLS], 今, 天, 新, 情, 很, 好, 。, [SEP]]
    今天新情很好。 => 今天心情很好。 新,心,2,3
    
    String text = "你找到你最喜欢的工作，我也很高心。";
    
    tokens -> [[CLS], 你, 找, 到, 你, 最, 喜, 欢, 的, 工, 作, ，, 我, 也, 很, 高, 心, 。, [SEP]]
    你找到你最喜欢的工作，我也很高心。 => 你找到你最喜欢的工作，我也很高兴。 心,兴,15,16
   ```

## Todo

此项目会持续优化，后期会持续尝试加入一些其它的纠错模型，特别是深度学习方面。

(1).升级有漏洞的jar包

(2).剔除长期没更新有漏洞的jar包，重写相关方法

(3).对berttokenizer进行改写

## contact

如有搜索、推荐、nlp以及大数据挖掘等问题或合作，可联系我：

1、我的github项目介绍：https://github.com/jiangnanboy

2、我的博客园技术博客：https://www.cnblogs.com/little-horse/

3、我的QQ号:2229029156

## Cite

如果你在研究中使用了jcorrector，请按如下格式引用：

```latex
@{jcorrector,
  author = {Shi Yan},
  title = {jcorrector: Text Error Correction Tool},
  year = {2022},
  url = {https://github.com/jiangnanboy/jcorrector},
}
```

## License
jcorrector 的授权协议为 Apache License 2.0，可免费用做商业用途。请在产品说明中附加jcorrector的链接和授权协议。

## Reference
https://github.com/shibing624/pycorrector
