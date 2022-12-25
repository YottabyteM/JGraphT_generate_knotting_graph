# 使用JgraphT完成无向图knotting_graph的转化
这个我搜遍全网，中国的外国的都找了，都没找到，代码也算是原创吧。但是因为Java语法不是很熟，凭着在AcWing刷了十来个Java语法题的“坚实功底”，写了这样一份略显答辩的代码。。基本上Java的优点是一点没体现，日后可能会修改一下。
## JgraphT的使用

### 图的声明和边的插入

我觉得没什么特别难的地方，可能创建图是挺省事的，但是具体遍历什么的还是不太适应，日后有机会再修改一下。 比如下面这一段就是声明了一个有向图。
```Java
Graph<String, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
```
然后如果需要加边的话也很简单，就是直接下面这种方式
```java
g.addVertex(v1);
g.addVertex(v2);
g.addEdge(v1, v2);
```
当然图论这些都是很基础的东西，我们具体解决某些问题的时候，更多的是需要用到点和邻点，边和邻边这些，这个我觉得是使用JgraphT最大的不方便，对于我这种用惯自己链式前向星的人来说，写起来确实有点麻烦。比如有如下遍历方式

### 图的遍历

```java
Iterator<String> iterator = new BreadthFirstIterator<>(G, start);
while (iterator.hasNext()) {
    String v = iterator.next();
}
```
表面看上去还是挺方便的，直接省的我们写BFS了，然而我觉得这个粒度还是不够小，比如说我在生成`knotting graph`的时候需要遍历其邻边，这就没办法通过这个BFS迭代器实现(真的没有吗，其实是有的，就是判断一下是不是和起点存在边，但是这个就很浪费时间了)，我选择通过下面这种方式访问某个点的所有邻边
```java
for (DefaultEdge e : G.edgesOf(ver)) {
    String cur = G.getEdgeTarget(e).equals(ver) ? G.getEdgeSource(e) : G.getEdgeTarget(e);
    .......
}
```

我猜测无向图里面通过edgesOf调用出来的这些边的Target应该是它的邻边，但是我也不敢确定是不是就是它，所以就用了这么个式子。

### 一些高级的算法

我看教程的时候确实说的很不错，各种最短路什么的都已经给实现好了，只不过我实现的这个算法确实没什么需要最短路这些的，这一部分就不说了，有需要的自己去[官网](https://jgrapht.org/guide/UserOverview)看一下，我觉得对于Java选手可能还是很友好的，毕竟我现在想如果让我来写连向星我对于数组的声明位置会头疼(别骂了，联系时长两个半小时的Java废狗)。

## knotting graph的实现

`knotting graph`(结图)这个玩意，简言之是图论里面一条性质，主要是用过以下的步骤生成的

1. 先把无向图的补图找出来，然后找到所有的连通分支。
2. 将无向图每个点分裂成这个点的邻点在其补图中的所处于的连通分支(不重复)。
3. 按照原图的相邻关系把这些点连起来。

不过在这之前，我们需要知道的是有些图是不能生成结图的，所以我们需要先判断能不能生成，一种方法是不判断，让算法执行，等到超时或者返回null的时候就行了, 另一种是利用图论中的结论，当图为`comparability graphs`时，就可以生成结图。从运行的角度来说，后者更节省，避免了死循环或者null等错误，从开发者的角度来说，我觉得判断`comparability graphs`代价也挺高的(对coder来说)。那么，废话不多说，我们给出判断`comparability graphs`的方法：

- 找到图中的蕴含类
- 遍历蕴含类中的集合
- 如存在某个蕴含类满足$B = B^{-1}$，那么不是`comparability graphs`
- 如果算法正常结束，那么说明是可以的

当然由于这一部分我写了四五个函数，所以就不多说明了，之后改了再填坑。

### 求解蕴含类

这个没啥可说的，就是理论知识的模拟，首先，我们根据$\Gamma$规则，求解出所有边对应的一个$\Gamma$矩阵，然后给这个矩阵求一下闭包，就可以直接得到蕴含类了，那这一部分都是模拟的算法，比较有趣的是这个求闭包的算法，应该是大二上离散学的，只是当时我离散学成一团浆糊，所以今天看到就会觉得非常得神奇。。。。

```java
public static void get_closure(int[][] a, int size)
{
    for (int i = 0; i < size; i ++ )
        for (int j = 0; j < size; j ++ )
        {
            if (a[j][i] != 0)
                for (int k = 0; k < size; k ++ )
                    a[j][k] = a[j][k] | a[i][k];
        }
}

public static Set<Set<DefaultEdge>> get_Implication_class(DirectedGraph<String, DefaultEdge> G) {
    Set<Set<DefaultEdge>> ans = new HashSet<Set<DefaultEdge>>();
    int Edge_num = G.edgeSet().size(), idx = 0;
    int[][] gamma = new int[Edge_num][Edge_num];
    DefaultEdge[] e = new DefaultEdge[Edge_num + 1];
    Iterator<DefaultEdge> it = G.edgeSet().iterator();
    while (it.hasNext()) {
        e[idx ++ ] = (DefaultEdge) it.next();
    }
    for (int i = 0; i < Edge_num; i ++ )
        for (int j = 0; j < Edge_num; j ++ )
            if (gamma[j][i] == 1 || gamma_Judge(e[i], e[j], G)) {
                gamma[i][j] = 1;
            }
    get_closure(gamma, Edge_num);
    for (int i = 0; i < Edge_num; i ++ )
        gamma[i][i] = 1;
    int cur = 0;
    while (cur < Edge_num){
        int nxt = cur;
        while (nxt < Edge_num) {
            if (nxt < Edge_num - 1)
                if (gamma[cur][nxt + 1] == 0)
                    break;
            nxt++;
        }
        Set <DefaultEdge> t = new HashSet<DefaultEdge>();
        for (int m = cur; m <= nxt; m ++ )
            if (e[m] != null)
                t.add(e[m]);
        ans.add(t);
        cur = nxt + 1;
    }

    return ans;
}
```

### 求解连通分支

判断玩是不是`comparability graphs`之后，我们就要开始正是干活了，首先我们先求出每个点的邻点所在的连通分支，那么这一个部分如果你每次去遍历找肯定有点慢，我们可以利用并查集的思想预先处理一下整个图，对于非根节点的图，我们也要维护一个他所在的集合，方便我们后续O(1)的时间调用。

```java
public static String find(String x, HashMap<String, String> p)
{
    if (!p.get(x).equals(x)) {
        String fa = p.get(x);
        p.put(x, find(fa, p));
    }
    return p.get(x);
}
```

在这个地方我写了俩并查集，但其实只有第一个外加上查询就可以实现了，当然用了第二个也并没有浪费特别多的时间，第一个`if`应该就会退出了吧。

然后就是一大坨的生成代码

```java
public static UndirectedGraph<String, DefaultEdge> generate_knotting_graph(UndirectedGraph<String, DefaultEdge> G) {
        UndirectedGraph<String, DefaultEdge> implement = get_implement(G);
        HashMap<String, Set<String>> components = Connection(implement);
        HashMap<String, Set<Set<String>>> split = new HashMap<>();
        for (String ver : G.vertexSet()) {
            for (DefaultEdge e : G.edgesOf(ver)) {
                String cur = G.getEdgeTarget(e).equals(ver) ? G.getEdgeSource(e) : G.getEdgeTarget(e);
                Set<String> s = components.get(cur);
                if (!split.containsKey(ver)) split.put(ver, new HashSet<>());
                if (!split.get(ver).contains(s))
                    split.get(ver).add(s);
            }
        }
        UndirectedGraph<String, DefaultEdge> ans = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
        HashMap<String, Set<String>> ver2con = new HashMap<>();
        for (String spl_v : split.keySet()) {
            int i = 1;
            for (Set<String> con : split.get(spl_v)) {
                String new_ver = spl_v + Integer.toString(i);
                i ++;
                ver2con.put(new_ver, con);
                ans.addVertex(new_ver);
            }
        }
        for (String ver : ans.vertexSet())
            for (String v : ans.vertexSet())
                if (v.charAt(0) != ver.charAt(0) && isConnected(ver2con.get(ver), ver2con.get(v), G, ver.charAt(0), v.charAt(0))) {
                    ans.addEdge(ver, v);
                }
        return ans;
}
```

### 二分性和连通性

连通性不必多说，我们直接遍历一遍图，然后看看遍历了几个点，只要少于总的点数肯定就是非连通的

```java
public static boolean Judge_connection(UndirectedGraph<String, DefaultEdge> G, String start) {
        Iterator<String> iterator = new BreadthFirstIterator<>(G, start);
        int num = 0;
        while (iterator.hasNext()) {
            String v = iterator.next();
            num ++;
        }
        return num == G.vertexSet().size();
    }
```

对于二分性的话，我们也可以直接使用染色法直接递归判断(网络流也可以，只不过写到这里实在是写不动了，要是用C++直接分分钟秒了)

```java
public static boolean dfs_Bipartite(UndirectedGraph<String, DefaultEdge>G, String cur_ver, int color, HashMap<String, Integer> colors) {
        colors.put(cur_ver, color);
        Iterator<String> iterator = new BreadthFirstIterator<>(G, cur_ver);
        while (iterator.hasNext()) {
            String nxt = iterator.next();
            if (!colors.containsKey(nxt)) {
                if (!dfs_Bipartite(G, nxt, 3 - color, colors))
                    return false;
            }
            else if (colors.get(nxt) == color)
                return false;
        }
        return true;
    }
    public static boolean Judge_Bipartite(UndirectedGraph<String, DefaultEdge> G) {
        HashMap<String, Integer> color = new HashMap<>();
        Iterator<String> it = G.vertexSet().iterator();
        while (it.hasNext()) {
            String ver = it.next();
            if (!color.containsKey(ver)) {
                if (!dfs_Bipartite(G, ver, 1, color)) {
                    return false;
                }
            }
        }
        return true;
    }
```

到这我的代码主体就结束了，剩下的都是一些简单的模拟，没什么营养

## 总结

1. Java博大精深
2. 还是C++好写
3. 图论博大精深
