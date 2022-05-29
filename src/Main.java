import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Main
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException
    {
        Index index = new Index();
        StringBuilder phrase;
        List<String> operators = new ArrayList<String>();
        operators.add("and");
        operators.add("or");
        operators.add("not");
        /**/
        index.buildIndex(new String[]{
                "file1.txt", "file2.txt", "file3.txt"});
        do
        {
            System.out.println("Print search phrase: ");
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            // Normalize words to handle upper, lower case
            phrase = new StringBuilder(in.readLine().toLowerCase());

            // Remove all words that not exist in the index
            String[] separated_phrase = phrase.toString().split("\\W+");
            phrase = new StringBuilder("");

            // flag to identify if phrase has indexed words except operators words
            boolean has_indexed_words = false;

            for (String word : separated_phrase)
            {
                if (index.index.containsKey(word))
                {
                    has_indexed_words = true;

                    if (!phrase.isEmpty()) phrase.append(" ");

                    phrase.append(word);
                }
                else if (operators.contains(word))
                {
                    if (!phrase.isEmpty()) phrase.append(" ");
                    phrase.append(word);
                }
            }

            // Split cleaned phrase into words
            separated_phrase = phrase.toString().split("\\W+");

            if (has_indexed_words)
            {
                System.out.println("\ninput after cleaning (indexed words only):\n\t\"" + phrase + "\"");
                HashSet<Integer> last_result;

                // In case of (not word) as input phrase
                if (separated_phrase.length == 2 && separated_phrase[0].contentEquals("not"))
                {
                    HashSet<Integer> src = new HashSet<Integer>(index.sources.keySet());
                    last_result = index.difference(src, index.index.get(separated_phrase[1]).postingList);
                }

                else
                {
                    last_result = index.index.get(separated_phrase[0]).postingList;
                    for (int i = 1; i < separated_phrase.length - 1; i++)
                    {
                        if (separated_phrase[i].equals("and") && separated_phrase[i + 1].equals("not"))
                        {
                            HashSet<Integer> postingList = index.index.get(separated_phrase[i + 2]).postingList;
                            last_result = index.andNot(last_result, postingList);
                            i++;
                        } else if (separated_phrase[i].equals("and"))
                        {
                            HashSet<Integer> postingList = index.index.get(separated_phrase[i + 1]).postingList;
                            last_result = index.intersect(last_result, postingList);
                        } else if (separated_phrase[i].equals("or") && separated_phrase[i + 1].equals("not"))
                        {
                            HashSet<Integer> postingList = index.index.get(separated_phrase[i + 2]).postingList;
                            last_result = index.orNot(last_result, postingList);
                            i++;
                        } else if (separated_phrase[i].equals("or"))
                        {
                            HashSet<Integer> postingList = index.index.get(separated_phrase[i + 1]).postingList;
                            last_result = index.union(last_result, postingList);
                        } else if (separated_phrase[i].equals("not"))
                        {
                            HashSet<Integer> postingList = index.index.get(separated_phrase[i + 1]).postingList;
                            last_result = index.difference(last_result, postingList);
                        }
                    }
                }
                System.out.println("******************************");
                // Result of phrase
                System.out.println("Found in:");
                for (Integer num : last_result)
                    System.out.println("\t" + index.sources.get(num));

                // Delete all non-indexed operators from phrase
                separated_phrase = phrase.toString().split("\\W+");
                phrase = new StringBuilder("");
                for (String word : separated_phrase)
                {
                    if (!index.index.containsKey(word))
                        continue;

                    if(!phrase.isEmpty()) phrase.append(" ");
                    phrase.append(word);
                }
                System.out.println("******************************");

                // Ranked Jaccard Similarity Search
                System.out.println("Ranked Jaccard Similarity Search Result:");
                index.get_jaccard_search(String.valueOf(phrase));

                System.out.println("******************************");

                // Ranked Cosine Similarity Search
                System.out.println("Ranked Cosine Similarity Search Result:");
                index.get_cosine_search(String.valueOf(phrase));
            }

            else System.out.println("\n--- non-indexed input ! ---\n");
            System.out.println("******************************");
        } while (true);

    }
}
