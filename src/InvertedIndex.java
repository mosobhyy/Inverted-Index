import java.io.*;
import java.util.*;

//=====================================================================
class DictEntry
{

    public int doc_freq = 0; // number of documents that contain the term
    public int term_freq = 0; //number of times the term is mentioned in the collection
    public HashSet<Integer> postingList;

    DictEntry()
    {
        postingList = new HashSet<Integer>();
    }
}

//=====================================================================
class Index
{

    //--------------------------------------------
    Map<Integer, String> sources;  // store the doc_id and the file name
    HashMap<String, DictEntry> index; // THe inverted index
    //--------------------------------------------

    Index()
    {
        sources = new HashMap<Integer, String>();
        index = new HashMap<String, DictEntry>();
    }

    //---------------------------------------------
    public void printPostingList(HashSet<Integer> hset)
    {
        for (Integer id : hset) System.out.print(id + ", ");
        System.out.println("");
    }

    public void printDictionary()
    {
        Iterator it = index.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry pair = (Map.Entry) it.next();
            DictEntry dd = (DictEntry) pair.getValue();
            System.out.print("** [" + pair.getKey() + "," + dd.doc_freq + "] <" + dd.term_freq + "> =--> ");
            //it.remove(); // avoids a ConcurrentModificationException
            printPostingList(dd.postingList);
        }
        System.out.println("------------------------------------------------------");
        System.out.println("*****    Number of terms = " + index.size());
        System.out.println("------------------------------------------------------");

    }

    //-----------------------------------------------
    public void buildIndex(String[] files)
    {
        int i = 0;
        for (String fileName : files)
        {
            try (BufferedReader file = new BufferedReader(new FileReader(fileName)))
            {
                sources.put(i, fileName);
                String ln;
                while ((ln = file.readLine()) != null)
                {
                    String[] words = ln.split("\\W+");
                    for (String word : words)
                    {
                        word = word.toLowerCase();
                        // check to see if the word is not in the dictionary
                        if (!index.containsKey(word))
                        {
                            index.put(word, new DictEntry());
                        }
                        // add document id to the posting list
                        if (!index.get(word).postingList.contains(i))
                        {
                            index.get(word).doc_freq += 1; //set doc freq to the number of doc that contain the term 
                            index.get(word).postingList.add(i); // add the posting to the posting:ist
                        }
                        //set the term_fteq in the collection
                        index.get(word).term_freq += 1;
                    }
                }
                // Remove empty word from index ("")
                index.remove("");

            } catch (IOException e)
            {
                System.out.println("File " + fileName + " not found. Skip it");
            }
            i++;
        }
        printDictionary();
    }

    //--------------------------------------------------------------------------
    // query inverted index
    // takes a string of terms as an argument
    public String find(String phrase)
    {
        String result = "";
        String[] words = phrase.toLowerCase().toLowerCase().split("\\W+");
        int len = words.length;

        HashSet<Integer> res = new HashSet<Integer>(index.get(words[0].toLowerCase()).postingList);
        for (String word : words)
        {
            res.retainAll(index.get(word).postingList);
        }
        for (int num : res)
        {
            result += "\t" + sources.get(num) + "\n";
        }
        return result;
    }

    //----------------------------------------------------------------------------  
    HashSet<Integer> intersect(HashSet<Integer> pL1, HashSet<Integer> pL2)
    {
        HashSet<Integer> answer = new HashSet<Integer>();
        ;
        Iterator<Integer> itP1 = pL1.iterator();
        Iterator<Integer> itP2 = pL2.iterator();
        int docId1 = 0, docId2 = 0;
//        INTERSECT ( p1 , p2 )
//          1 answer ←   {}
        // answer =
//          2 while p1  != NIL and p2  != NIL
        if (itP1.hasNext())
            docId1 = itP1.next();
        if (itP2.hasNext())
            docId2 = itP2.next();

        while (itP1.hasNext() && itP2.hasNext())
        {

//          3 do if docID ( p 1 ) = docID ( p2 )
            if (docId1 == docId2)
            {
//          4   then ADD ( answer, docID ( p1 ))
//          5       p1 ← next ( p1 )
//          6       p2 ← next ( p2 )
                answer.add(docId1);
                docId1 = itP1.next();
                docId2 = itP2.next();
            } //          7   else if docID ( p1 ) < docID ( p2 )
            //          8        then p1 ← next ( p1 )
            else if (docId1 < docId2)
            {
                if (itP1.hasNext())
                    docId1 = itP1.next();
                else return answer;

            } else
            {
//          9        else p2 ← next ( p2 )
                if (itP2.hasNext())
                    docId2 = itP2.next();
                else return answer;

            }

        }
        if (docId1 == docId2)
        {
            answer.add(docId1);
        }

//          10 return answer
        return answer;
    }
    //-----------------------------------------------------------------------

    //----------------------------------------------------------------------------
    HashSet<Integer> union(HashSet<Integer> pL1, HashSet<Integer> pL2)
    {
        HashSet<Integer> answer = new HashSet<>();
        Iterator<Integer> itP1 = pL1.iterator();
        Iterator<Integer> itP2 = pL2.iterator();

        while (itP1.hasNext())
            answer.add(itP1.next());

        while (itP2.hasNext())
            answer.add(itP2.next());

        return answer;
    }
    //-----------------------------------------------------------------------

    //----------------------------------------------------------------------------
    HashSet<Integer> difference(HashSet<Integer> pL1, HashSet<Integer> pL2)
    {

        // In case of pL1 was empty, it was raise an error, so we add this condition.
        if (pL1.isEmpty() || pL2.isEmpty())
            return pL1;

        HashSet<Integer> answer = new HashSet<Integer>();
        Iterator<Integer> itP1 = pL1.iterator();
        Iterator<Integer> itP2 = pL2.iterator();

        int docId1 = itP1.next(), docId2 = itP2.next();

        while (itP1.hasNext() && itP2.hasNext())
        {
            if (docId1 == docId2)
            {
                docId1 = itP1.next();
                docId2 = itP2.next();
            } else if (docId1 < docId2)
            {
                answer.add(docId1);
                docId1 = itP1.next();
            } else docId2 = itP2.next();
        }

        // check last element because while loop break before checking it
//        if (docId1 != docId2 && itP1.hasNext())
        if (docId1 != docId2)
            answer.add(itP1.next());

        // Add all remaining ids in pL1
        while (itP1.hasNext())
            answer.add(itP1.next());

        return answer;
    }

    //----------------------------------------------------------------------------
    HashSet<Integer> andNot(HashSet<Integer> pL1, HashSet<Integer> pL2)
    {
        HashSet<Integer> src = new HashSet<Integer>(sources.keySet());
        HashSet<Integer> not_pL2 = difference(src, pL2);
        return intersect(pL1, not_pL2);
    }

    //----------------------------------------------------------------------------
    HashSet<Integer> orNot(HashSet<Integer> pL1, HashSet<Integer> pL2)
    {
        HashSet<Integer> src = new HashSet<Integer>(sources.keySet());
        HashSet<Integer> not_pL2 = difference(src, pL2);
        return union(pL1, not_pL2);
    }
    //-----------------------------------------------------------------------

    public String find_01(String phrase)
    { // 2 term phrase  2 postingsLists
        String result = "";
        String[] words = phrase.toLowerCase().split("\\W+");
        // 1- get first posting list
        HashSet<Integer> pL1 = new HashSet<Integer>(index.get(words[0].toLowerCase()).postingList);
        // 2- get second posting list
        HashSet<Integer> pL2 = new HashSet<Integer>(index.get(words[1].toLowerCase()).postingList);

        // 3- apply the algorithm
        HashSet<Integer> answer = intersect(pL1, pL2);

        result = "Found in: \n";
        for (int num : answer)
        {
            result += "\t" + sources.get(num) + "\n";
        }
        return result;
    }
//-----------------------------------------------------------------------

    public String find_02(String phrase)
    { // 3 lists

        String result = "";
        String[] words = phrase.toLowerCase().split("\\W+");
        HashSet<Integer> pL1 = new HashSet<Integer>(index.get(words[0].toLowerCase()).postingList);
        //printPostingList(pL1);
        HashSet<Integer> pL2 = new HashSet<Integer>(index.get(words[1].toLowerCase()).postingList);
        HashSet<Integer> answer1 = intersect(pL1, pL2);
        HashSet<Integer> pL3 = new HashSet<Integer>(index.get(words[2].toLowerCase()).postingList);
        HashSet<Integer> answer2 = intersect(pL3, answer1);

        for (int num : answer2)
        {
            result += "\t" + sources.get(num) + "\n";
        }
        return result;

    }
    //-----------------------------------------------------------------------

    public String find_03(String phrase)
    { // any mumber of terms non-optimized search
        String result = "";
        String[] words = phrase.toLowerCase().split("\\W+");
        int len = words.length;

        HashSet<Integer> res = new HashSet<Integer>(index.get(words[0].toLowerCase()).postingList);
        int i = 1;
        while (i < len)
        {
            res = intersect(res, index.get(words[i].toLowerCase()).postingList);
            i++;
        }
        for (int num : res)
        {
            result += "\t" + sources.get(num) + "\n";
        }
        return result;
    }

    //-----------------------------------------------------------------------

    public HashSet<Integer> find_03_union(String phrase)
    { // any mumber of terms non-optimized search
        String[] words = phrase.toLowerCase().split("\\W+");
        int len = words.length;

        HashSet<Integer> result = new HashSet<Integer>(index.get(words[0].toLowerCase()).postingList);
        int i = 1;
        while (i < len)
        {
            result = union(result, index.get(words[i].toLowerCase()).postingList);
            i++;
        }
        return result;
    }

    //-----------------------------------------------------------------------
    String[] rearrange(String[] words, int[] freq, int len)
    {
        boolean sorted = false;
        int temp;
        String sTmp;

        int c = 0;
        for (String word : words)
        {
            if (index.containsKey(word))
            {
                freq[c] = index.get(word.toLowerCase()).doc_freq;
                c++;
            }
        }
        //-------------------------------------------------------
        while (!sorted)
        {
            sorted = true;
            for (int i = 0; i < len - 1; i++)
            {
                if (freq[i] > freq[i + 1])
                {
                    temp = freq[i];
                    sTmp = words[i];
                    freq[i] = freq[i + 1];
                    words[i] = words[i + 1];
                    freq[i + 1] = temp;
                    words[i + 1] = sTmp;
                    sorted = false;
                }
            }
        }
        return words;
    }

    //-----------------------------------------------------------------------         
    public String find_04(String phrase)
    { // any mumber of terms optimized search
        String result = "";
        String[] words = phrase.toLowerCase().split("\\W+");
        int len = words.length;
        words = rearrange(words, new int[len], len);

        HashSet<Integer> res = new HashSet<Integer>(index.get(words[0].toLowerCase()).postingList);
        int i = 1;
        while (i < len)
        {
            res = intersect(res, index.get(words[i].toLowerCase()).postingList);
            i++;
        }
        for (int num : res)
        {
            result += "\t" + sources.get(num) + "\n";
        }
        return result;
    }
    //-----------------------------------------------------------------------

    private double jaccard_similarity(String phrase, String fileName)
    {
        // Store all unique words in the document
        HashSet<String> doc_words = new HashSet<String>();

        // Get all unique words in the document
        try (BufferedReader file = new BufferedReader(new FileReader(fileName)))
        {
            String ln;
            while ((ln = file.readLine()) != null)
            {
                String[] words = ln.toLowerCase().split("\\W+");
                doc_words.addAll(Arrays.asList(words));
            }
            // Remove empty word from index ("")
            doc_words.remove("");
        } catch (IOException e)
        {
            System.out.println("File " + fileName + " not found. Skip it");
        }

        // Get number of intersected words
        String[] phrase_words = phrase.toLowerCase().split("\\W+");
        int intersection_count = 0;
        for (String word : phrase_words)
        {
            if (doc_words.contains(word))
                intersection_count++;
        }

        // Merge phrase words with doc words to get union
        doc_words.addAll(Arrays.asList(phrase_words));

        // Calculate jaccard similarity (intersection / union)
        double result = (double) intersection_count / doc_words.size();

        // Set 5 precision
        result = Math.round(result * 100000) / 100000.0;
        return result;
    }
    //-----------------------------------------------------------------------

    // function to sort hashmap by values
    private HashMap<String, Double> sortByValue(HashMap<String, Double> hm)
    {
        // Create a list from elements of HashMap
        List<Map.Entry<String, Double>> list =
                new LinkedList<Map.Entry<String, Double>>(hm.entrySet());

        // Sort the list
        Collections.sort(list, new Comparator<Map.Entry<String, Double>>()
        {
            public int compare(Map.Entry<String, Double> o1,
                               Map.Entry<String, Double> o2)
            {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        // put data from sorted list to hashmap
        HashMap<String, Double> temp = new LinkedHashMap<String, Double>();
        for (Map.Entry<String, Double> aa : list)
        {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }

    //-----------------------------------------------------------------------

    private HashMap<String, Double> ranked_jaccard_search(String phrase)
    {
        HashMap<String, Double> ranked_results = new HashMap<>();
        // Get all relevant docs
        HashSet<Integer> docsIds = find_03_union(phrase);

        if (docsIds.isEmpty())
            return ranked_results;

        for (int docId : docsIds)
        {
            String docName = sources.get(docId);
            double similarity = jaccard_similarity(phrase, docName);
            ranked_results.put(docName, similarity);
        }

        // Sort results in descending order
        ranked_results = sortByValue(ranked_results);

        return ranked_results;
    }

    public void get_jaccard_search(String phrase)
    {
        HashMap<String, Double> ranked_results = ranked_jaccard_search(phrase);

        for (HashMap.Entry<String, Double> entry : ranked_results.entrySet())
        {
            System.out.println("\t" + entry.getKey() + " ----> " + entry.getValue());
        }
    }
}

//=====================================================================