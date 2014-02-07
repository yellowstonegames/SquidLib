package squidpony.squidtext.namegen.weightedletter;

/**
 * Based on work by Nolithius available at the following two sites
 * https://github.com/Nolithius/weighted-letter-namegen
 * http://code.google.com/p/weighted-letter-namegen/
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
	public class WeightedLetterNamegen
	{
		public static const LAST_LETTER_CANDIDATES_MAX:int = 5;
		
		private var initialized:Boolean = false;
		
		private var names:Array;
		private var sizes:Array;
		
		private var letters:Dictionary;
		private var firstLetterSamples:Array;
		private var lastLetterSamples:Array;
		
		
		public function WeightedLetterNamegen(p_names:Array)
		{
			names = p_names;
		}
		
		
		/**
		 * Initialization, statistically measures letter likelyhood. Called by generate() the first time, or can be called manually.
		 */
		public function init():void
		{
			sizes = new Array();
			letters = new Dictionary();
			firstLetterSamples = new Array();
			lastLetterSamples = new Array();			
						
			for (var i:uint = 0; i < names.length; i++)
			{
				var name:Array = names[i].split("");
												
				// (1) Insert size
				sizes.push(name.length);
				
				// (2) Grab first letter
				firstLetterSamples.push(name[0]);

				// (3) Grab last letter
				lastLetterSamples.push(name[name.length - 1]);
				
				// (4) Process all letters
				for (var n:uint = 0; n < name.length -1; n++)
				{
					var letter:String = name[n];
					var nextLetter:String = name[n + 1];
					
					// Create letter if it doesn't exist
					if (!letters[letter])
					{
						letters[letter] = new WeightedLetter(letter);
					}
					
					letters[letter].addNextLetter(nextLetter);
					
					// If letter was uppercase (beginning of name), also add a lowercase entry
					if (letter != letter.toLowerCase())
					{
						letter = letter.toLowerCase();
						
						// Create letter if it doesn't exist
						if (!letters[letter])
						{
							letters[letter] = new WeightedLetter(letter);
						}
						
						letters[letter].addNextLetter(nextLetter);
					}
				}
			}


			for each (var weightedLetter:WeightedLetter in letters)
			{
				// Expand letters into samples
				weightedLetter.nextLetters.expandSamples();
			}
			
			initialized = true;
		}
		
		
		public function generate(amountToGenerate:uint = 1):Array
		{
			// Initialize if called for the first time
			if (!initialized) init();
			
			var result:Array = new Array();
			
			for (var nameCount:uint = 0; nameCount < amountToGenerate;)
			{
				var name:Array = new Array();
				
				// Pick size
				var size:int = pickRandomElementFromArray(sizes) as int;
				
				// Pick first letter
				var firstLetter:String = pickRandomElementFromArray(firstLetterSamples);
				
				name.push(firstLetter);
				
				for (var i:int = 1; i < size-2; i++)
				{
					// Only continue if the last letter added was non-null
					if (name[i - 1])
					{
						name.push(getRandomNextLetter(name[i - 1]));
					}
					else
					{
						break;
					}
				}

				// Attempt to find a last letter
				for (var lastLetterFits:int = 0; lastLetterFits < LAST_LETTER_CANDIDATES_MAX; lastLetterFits++)
				{
					var lastLetter:String = pickRandomElementFromArray(lastLetterSamples);				
					var intermediateLetterCandidate:String = getIntermediateLetter(name[name.length - 1], lastLetter);
					
					// Only attach last letter if the candidate is valid (if no candidate, the antepenultimate letter always occurs at the end)
					if (intermediateLetterCandidate)
					{
						name.push(intermediateLetterCandidate);
						name.push(lastLetter);
						break;
					}
				}

				var nameString:String = name.join("");
				
				// Check that the word has no triple letter sequences, and that the Levenshtein distance is kosher
				if (tripleLetterCheck(name) && checkLevenshtein(nameString))
				{
					result.push(nameString);
					
					// Only increase the counter if we've successfully added a name
					nameCount++
				}
			}
			
			return result;
		}
		
		
		/**
		 * Searches for the best fit letter between the letter before and the letter after (non-random).
		 * Used to determine penultimate letters in names.
		 * @param	letterBefore	The letter before the desired letter.
		 * @param	letterAfter		The letter after the desired letter.
		 * @return	The best fit letter between the provided letters.
		 */
		private function getIntermediateLetter(letterBefore:String, letterAfter:String):String
		{
			if(letterBefore && letterAfter)
			{
				// First grab all letters that come after the 'letterBefore'
				var letterCandidates:Dictionary = letters[letterBefore].nextLetters.letters;
				
				var bestFitLetter:String = null;
				var bestFitScore:uint = 0;
				
				// Step through candidates, and return best scoring letter
				for (var letter in letterCandidates)
				{
					var weightedLetterGroup:WeightedLetterGroup = letters[letter].nextLetters;
					var letterCounter:WeightedLetterCounter = weightedLetterGroup.letters[letterAfter];
					
					if (letterCounter)
					{
						if (letterCounter.count > bestFitScore)
						{
							bestFitLetter = letter;
							bestFitScore = letterCounter.count;
						}
					}
				}
				
				return bestFitLetter;
			}
			else
			{
				// If any of the passed parameters were null, return null. This happens when the letterBefore has no candidates.
				return null;
			}
		}
		
		
		/**
		 * Checks that no three letters happen in succession.
		 * @param	name	The name array (easier to iterate)
		 * @return	True if no triple letter sequence is found.
		 */
		private function tripleLetterCheck(name:Array):Boolean
		{
			for (var i:uint = 2; i < name.length; i++)
			{
				if (name[i] == name[i - 1] && name[i] == name[i - 2])
				{
					return false;
				}
			}
			
			return true;
		}
		
		
		/**
		 * Checks that the Damerau-Levenshtein distance of this name is within a given bias from a name on the master list.
		 * @param	name	The name string.
		 * @return	True if a name is found that is within the bias.
		 */
		private function checkLevenshtein(name:String):Boolean
		{
			var levenshteinBias:uint = uint(name.length / 2);
			
			// Grab the closest matches, just for fun
			var closestName:String = "";
			var closestDistance:uint = uint.MAX_VALUE;
			
			for (var i:uint = 0; i < names.length; i++)
			{
				var levenshteinDistance:uint = StringUtils.damerau(name, names[i]);
				
				// This is just to get an idea of what is failing
				if (levenshteinDistance < closestDistance)
				{
					closestDistance = levenshteinDistance;
					closestName = names[i];
				}
				
				if (levenshteinDistance <= levenshteinBias)
				{
					return true;
				}
			}
			
			return false;
		}
		
		
		private function pickRandomElementFromArray(array:Array):*
		{
			return array[Math.round(Math.random() * (array.length - 1))];
		}
		
		
		private function getRandomNextLetter(letter:String):String
		{
			var weightedLetter:WeightedLetter = letters[letter];
			
			return pickRandomElementFromArray(weightedLetter.nextLetters.letterSamples) as String;
		}
	}
}
