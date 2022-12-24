import java.util.*;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.alg.*;
import org.jgrapht.traverse.BreadthFirstIterator;

public class GraphTest
{
    public static void main(String[] args)
    {
        DirectedGraph<String, DefaultEdge> stringGraph = createStringGraph();
//        Connection(Directed2Undirected(stringGraph));
//        System.out.println(Judge_binary(Directed2Undirected(stringGraph)));
//        Iterator<Set<DefaultEdge>> it = get_Implication_class(stringGraph).iterator();
//        while (it.hasNext()) {
//            System.out.print("{ ");
//            Iterator<DefaultEdge> itt = it.next().iterator();
//            while (itt.hasNext()) {
//                System.out.print(itt.next());
//                if (itt.hasNext())
//                    System.out.print(',');
//            }
//            System.out.print("}\n");
//        }
        if (!Judge_comparability(stringGraph)) {
            System.out.println("不是可比性图\n");
            return;
        }
        UndirectedGraph<String, DefaultEdge> knotting_graph = generate_knotting_graph(Directed2Undirected(stringGraph));
        if (Judge_connection(knotting_graph, knotting_graph.vertexSet().iterator().next())) {
            System.out.println("是连通图\n");
        }
        else {
            System.out.println("不是连通图\n");
        }
        if (Judge_Bipartite(knotting_graph)) {
            System.out.println("是二分图\n");
        }
        else {
            System.out.println("不是二分图\n");
        }
    }

    // 求解闭包用的
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
    // 判断是否满足gamma规则
    public static boolean is_connected(String a_1, String b_1, String a_2, String b_2, DirectedGraph<String, DefaultEdge> G)
    {
        if (a_1.equals(b_1)) {
            return !G.containsEdge(a_2, b_2);
        } else if (a_1.equals(b_2)) {
            return is_connected(a_1, b_2, a_2, b_1, G);
        } else if (a_2.equals(b_1)) {
            return is_connected(a_2, b_1, a_1, b_2, G);
        } else if (a_2.equals(b_2)) {
            return is_connected(a_2, b_2, a_1, b_1, G);
        }
        return false;
    }

    // 判断是否满足gamma规则
    public static boolean gamma_Judge(DefaultEdge a, DefaultEdge b, DirectedGraph<String, DefaultEdge> G)
    {
        String a_1 = G.getEdgeSource(a), a_2 = G.getEdgeTarget(a);
        String b_1 = G.getEdgeSource(b), b_2 = G.getEdgeTarget(b);
        if (!a_1.equals(b_1) && !a_2.equals(b_1) && !a_1.equals(b_2) && !(a_2.equals(b_2)))
            return false;
        return is_connected(a_1, b_1, a_2, b_2, G);
    }

    // 得到蕴含类
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

    // 判断一下蕴含类和蕴含类的逆是否有交集
    public static boolean is_verse(DefaultEdge a, DefaultEdge b, DirectedGraph<String, DefaultEdge> G) {
        return G.getEdgeSource(a).equals(G.getEdgeTarget(b)) && G.getEdgeTarget(a).equals(G.getEdgeSource(b));
    }

    // 判断图是否为可比图
    public static boolean Judge_comparability(DirectedGraph<String, DefaultEdge> G) {
        Iterator <DefaultEdge> edgeit = G.edgeSet().iterator();
        if (get_Implication_class(G).size() == 1) return false;
        while (edgeit.hasNext()) {
            DefaultEdge e = edgeit.next();
            Iterator<Set<DefaultEdge>> it = get_Implication_class(G).iterator();
            while (it.hasNext()) {
                Set<DefaultEdge> cur = it.next();
                if (cur.contains(e))
                {
                    Iterator<DefaultEdge> itt = cur.iterator();
                    while (itt.hasNext()) {
                        DefaultEdge e_cur = itt.next();
                        Iterator<DefaultEdge> ittt = cur.iterator();
                        while (ittt.hasNext()) {
                            DefaultEdge e_nxt = ittt.next();
                            if (is_verse(e_cur, e_nxt, G))
                                return false;
                        }
                    }
                }
            }
        }
        return true;
    }
    public static boolean Judge_connection(UndirectedGraph<String, DefaultEdge> G, String start) {
        Iterator<String> iterator = new BreadthFirstIterator<>(G, start);
        int num = 0;
        while (iterator.hasNext()) {
            String v = iterator.next();
            num ++;
        }
        return num == G.vertexSet().size();
    }

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

    public static String find(String x, HashMap<String, String> p)
    {
        if (!p.get(x).equals(x)) {
            String fa = p.get(x);
            p.put(x, find(fa, p));
        }
        return p.get(x);
    }

    public static String find_Union(String ver, HashMap<String, String> p) {
        if (p.get(ver).equals(ver))
            return p.get(ver);
        return find_Union(p.get(ver), p);
    }

    public static HashMap<String, Set<String>> Connection(UndirectedGraph<String, DefaultEdge> G) {
        HashMap<String, Set<String>> ans = new HashMap<>();
        HashMap<String, String> p = new HashMap<>();
        for (String cur : G.vertexSet()) {
            p.put(cur, cur);
        }
        for (DefaultEdge cur : G.edgeSet()) {
            String st = G.getEdgeTarget(cur), ed = G.getEdgeSource(cur);
            p.remove(st);
            p.put(st, find(ed, p));
        }
        for (String v : G.vertexSet()) {
            if (p.get(v).equals(v)) {
                if (!ans.containsKey(v)) ans.put(v, new HashSet<>());
                ans.get(v).add(v);
            }
            else {
                String r = find_Union(v, p);
                if (!ans.containsKey(r)) ans.put(v, new HashSet<>());
                ans.get(r).add(v);
            }
        }
        for (String v : G.vertexSet()) {
            if (!p.get(v).equals(v)) {
                ans.put(v, ans.get(find_Union(v, p)));
            }
        }
        return ans;
    }

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
                ans.addVertex(new_ver);
            }
        }
        System.out.println(ans);
        return null;
    }

    public static UndirectedGraph<String, DefaultEdge> get_implement(UndirectedGraph<String, DefaultEdge> G) {
        UndirectedGraph<String, DefaultEdge> res = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
        for (String cur : G.vertexSet()) {
            res.addVertex(cur);
        }
        for (String v : G.vertexSet()) {
            for (String c : G.vertexSet()) {
                if (!G.containsEdge(v, c) && !res.containsEdge(v, c) && !v.equals(c)) {
                    res.addEdge(v, c);
                }
            }
        }
        return res;
    }
    public static UndirectedGraph<String, DefaultEdge> Directed2Undirected(DirectedGraph<String, DefaultEdge> G) {
        UndirectedGraph<String, DefaultEdge> ans = new SimpleGraph<>(DefaultEdge.class);
        for (String s : G.vertexSet()) {
            ans.addVertex(s);
        }
        for (DefaultEdge e : G.edgeSet()) {
            String st = G.getEdgeSource(e), ed = G.getEdgeTarget(e);
            if (!ans.containsEdge(st, ed)) {
                ans.addEdge(st, ed);
            }
        }
        return ans;
    }
    private static DirectedGraph<String, DefaultEdge> createStringGraph()
    {
//        Scanner sc = new Scanner(System.in);
        DirectedGraph<String, DefaultEdge> g = new DirectedMultigraph<String, DefaultEdge>(DefaultEdge.class);
/*
Sample
5 A B C D E
8
A B
B C
D E
A C
A D
C E
B E
B D
*/
//        int num = sc.nextInt();
//        for (int i = 0; i < num; i ++ )
//        {
//            String op = sc.next();
//            g.addVertex(op);
//        }
//        int m = sc.nextInt();
//        for (int i = 0; i < m; i ++ ) {
//            String op1 = sc.next();
//            String op2 = sc.next();
//            g.addEdge(op1, op2);
//        }
        String v1 = "A";
        String v2 = "B";
        String v3 = "C";
        String v4 = "D";
        String v5 = "E";
        String v6 = "F";
        String v7 = "G";
        String v8 = "H";

        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);
        g.addVertex(v4);
        g.addVertex(v5);
//        g.addVertex(v6);
//        g.addVertex(v7);
//        g.addVertex(v8);

//        g.addEdge(v1, v2);
//        g.addEdge(v2, v1);
//        g.addEdge(v1, v4);
//        g.addEdge(v4, v1);
//        g.addEdge(v1, v6);
//        g.addEdge(v6, v1);
//        g.addEdge(v3, v4);
//        g.addEdge(v4, v3);
//        g.addEdge(v3, v6);
//        g.addEdge(v6, v3);
//        g.addEdge(v5, v6);
//        g.addEdge(v6, v5);
//        g.addEdge(v6, v7);
//        g.addEdge(v7, v6);
//        g.addEdge(v7, v8);
//        g.addEdge(v8, v7);

        g.addEdge(v1, v2);
        g.addEdge(v2, v3);
        g.addEdge(v4, v5);
        g.addEdge(v1, v3);
        g.addEdge(v1, v4);
        g.addEdge(v3, v5);
        g.addEdge(v2, v5);
        g.addEdge(v2, v4);

        g.addEdge(v2, v1);
        g.addEdge(v3, v2);
        g.addEdge(v5, v4);
        g.addEdge(v3, v1);
        g.addEdge(v4, v1);
        g.addEdge(v5, v3);
        g.addEdge(v5, v2);
        g.addEdge(v4, v2);

//        g.addEdge(v1, v3);
//        g.addEdge(v1, v4);
//        g.addEdge(v1, v5);
//        g.addEdge(v1, v2);
//        g.addEdge(v3, v4);
//        g.addEdge(v3, v2);
//        g.addEdge(v4, v2);
//        g.addEdge(v5, v2);
//
//        g.addEdge(v3, v1);
//        g.addEdge(v4, v1);
//        g.addEdge(v5, v1);
//        g.addEdge(v2, v1);
//        g.addEdge(v4, v3);
//        g.addEdge(v2, v3);
//        g.addEdge(v2, v4);
//        g.addEdge(v2, v5);
        return g;
    }
}