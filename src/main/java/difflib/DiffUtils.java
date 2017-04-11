/*-
 * #%L
 * java-diff-utils
 * %%
 * Copyright (C) 2009 - 2017 java-diff-utils
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */
package difflib;

import difflib.algorithm.DiffAlgorithm;
import difflib.algorithm.DiffException;
import difflib.algorithm.myers.MyersDiff;
import difflib.patch.Delta;
import difflib.patch.Equalizer;
import difflib.patch.Patch;
import difflib.patch.PatchFailedException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import static java.util.stream.Collectors.joining;

/**
 * Implements the difference and patching engine
 *
 * @author <a href="dm.naumenko@gmail.com">Dmitry Naumenko</a>
 * @version 0.4.1
 */
public final class DiffUtils {

    /**
     * Computes the difference between the original and revised list of elements with default diff
     * algorithm
     *
     * @param original The original text. Must not be {@code null}.
     * @param revised The revised text. Must not be {@code null}.
     * @return The patch describing the difference between the original and revised sequences. Never
     * {@code null}.
     */
    public static <T> Patch<T> diff(List<T> original, List<T> revised) throws DiffException {
        return DiffUtils.diff(original, revised, new MyersDiff<>());
    }

    /**
     * Computes the difference between the original and revised list of elements with default diff
     * algorithm
     *
     * @param original The original text. Must not be {@code null}.
     * @param revised The revised text. Must not be {@code null}.
     *
     * @param equalizer the equalizer object to replace the default compare algorithm
     * (Object.equals). If {@code null} the default equalizer of the default algorithm is used..
     * @return The patch describing the difference between the original and revised sequences. Never
     * {@code null}.
     */
    public static <T> Patch<T> diff(List<T> original, List<T> revised,
            Equalizer<T> equalizer) throws DiffException {
        if (equalizer != null) {
            return DiffUtils.diff(original, revised,
                    new MyersDiff<>(equalizer));
        }
        return DiffUtils.diff(original, revised, new MyersDiff<>());
    }

    /**
     * Computes the difference between the original and revised list of elements with default diff
     * algorithm
     *
     * @param original The original text. Must not be {@code null}.
     * @param revised The revised text. Must not be {@code null}.
     * @param algorithm The diff algorithm. Must not be {@code null}.
     * @return The patch describing the difference between the original and revised sequences. Never
     * {@code null}.
     */
    public static <T> Patch<T> diff(List<T> original, List<T> revised,
            DiffAlgorithm<T> algorithm) throws DiffException {
        Objects.requireNonNull(original,"original must not be null");
        Objects.requireNonNull(revised,"revised must not be null");
        Objects.requireNonNull(algorithm,"algorithm must not be null");
        
        return algorithm.diff(original, revised);
    }

    /**
     * Computes the difference between the given texts inline. This one uses the "trick" to make out
     * of texts lists of characters, like DiffRowGenerator does and merges those changes at the end
     * together again.
     *
     * @param original
     * @param revised
     * @return
     */
    public static Patch<String> diffInline(String original, String revised) throws DiffException {
        LinkedList<String> origList = new LinkedList<>();
        LinkedList<String> revList = new LinkedList<>();
        for (Character character : original.toCharArray()) {
            origList.add(character.toString());
        }
        for (Character character : revised.toCharArray()) {
            revList.add(character.toString());
        }
        Patch<String> patch = DiffUtils.diff(origList, revList);
        for (Delta<String> delta : patch.getDeltas()) {
            delta.getOriginal().setLines(compressLines(delta.getOriginal().getLines(), ""));
            delta.getRevised().setLines(compressLines(delta.getRevised().getLines(), ""));
        }
        return patch;
    }

    private static List<String> compressLines(List<String> lines, String delimiter) {
        if (lines.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(lines.stream().collect(joining(delimiter)));
    }

    /**
     * Patch the original text with given patch
     *
     * @param original the original text
     * @param patch the given patch
     * @return the revised text
     * @throws PatchFailedException if can't apply patch
     */
    public static <T> List<T> patch(List<T> original, Patch<T> patch)
            throws PatchFailedException {
        return patch.applyTo(original);
    }

    /**
     * Unpatch the revised text for a given patch
     *
     * @param revised the revised text
     * @param patch the given patch
     * @return the original text
     */
    public static <T> List<T> unpatch(List<T> revised, Patch<T> patch) {
        return patch.restore(revised);
    }

    private DiffUtils() {
    }
}
