package squidpony.squidtext;

/**
 * Based on work by Nolithius available at the following two sites
 * https://github.com/Nolithius/weighted-letter-namegen
 * http://code.google.com/p/weighted-letter-namegen/
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */

	public class NamegenDemo extends MovieClip
	{
		public var seed:TextField;
		public var results:TextField;
		public var seedBackground:Sprite;
		public var resultsBackground:Sprite;
		public var blocker:Sprite;
		public var generateButton:Sprite;
		public var generateButtonLabel:TextField;
		public var seedEditCaption:TextField;
		
		public var namegen:WeightedLetterNamegen;
		
		
		public function NamegenDemo()
		{
			// Text formats
			var inputTextFormat:TextFormat = new TextFormat('Georgia', 14, 0x000000);
			var buttonTextFormat:TextFormat = new TextFormat('Georgia', 16, 0xffffff, false, false, false, null, null, TextFormatAlign.CENTER);
			var headingTextFormat:TextFormat = new TextFormat('Georgia', 18, 0x505050);
			var captionTextFormat:TextFormat = new TextFormat('Georgia', 14, 0x000000, false, true, false, null, null, TextFormatAlign.CENTER);
			
			// Headings
			var seedHeading:TextField = new TextField();
			seedHeading.defaultTextFormat = headingTextFormat;
			seedHeading.text = "Seed";
			seedHeading.x = 10;
			seedHeading.selectable = false;
			addChild(seedHeading);
			
			var resultsHeading:TextField = new TextField();
			resultsHeading.defaultTextFormat = headingTextFormat;
			resultsHeading.text = "Results";
			resultsHeading.x = 370;
			resultsHeading.selectable = false;
			addChild(resultsHeading);		
			
			// Seed edit caption
			seedEditCaption = new TextField();
			seedEditCaption.defaultTextFormat = captionTextFormat;
			seedEditCaption.text = "Click seed names to edit.";
			seedEditCaption.selectable = false;
			seedEditCaption.width = 200;
			seedEditCaption.x = 10;
			seedEditCaption.y = 300;
			addChild(seedEditCaption);
			
			// Results background
			resultsBackground = new Sprite();
			resultsBackground.graphics.beginFill(0xffffff);
			resultsBackground.graphics.drawRect(0, 0, 200, 280);
			resultsBackground.graphics.endFill();
			resultsBackground.x = 370;
			resultsBackground.y = 30;
			resultsBackground.alpha = 0.25;
			addChild(resultsBackground);
			
			// Results output textfield
			results = new TextField();
			results.defaultTextFormat = inputTextFormat;
			results.x = 370;
			results.y = 30;
			results.width = 200;
			results.height = 280;
			addChild(results);
			
			// Blocker
			blocker = new Sprite();
			blocker.graphics.beginFill(0xE4E4E4);
			blocker.graphics.drawRect(0, 0, 580, 320);
			blocker.graphics.endFill();
			blocker.alpha = 0.75;
			blocker.visible = false;
			addChild(blocker);
			
			// Generate button
			generateButton = new Sprite();
			generateButton.buttonMode = true;
			generateButton.mouseChildren = false;
			generateButton.graphics.beginFill(0x000000);
			generateButton.graphics.drawRect(0, 0, 140, 24);
			generateButton.graphics.endFill();
			generateButton.x = 220;
			generateButton.y = 150;
			generateButtonLabel = new TextField();
			generateButtonLabel.width = 140;
			generateButtonLabel.height = 24;
			generateButtonLabel.defaultTextFormat = buttonTextFormat;

			generateButtonLabel.selectable = false;
			generateButtonLabel.text = "Generate";
			generateButton.addChild(generateButtonLabel);		
			
			addChild(generateButton);
			generateButton.addEventListener(MouseEvent.CLICK, handleGenerateClick);
			
			// Seed background
			seedBackground = new Sprite();
			seedBackground.graphics.beginFill(0xffffff);
			seedBackground.graphics.drawRect(0, 0, 200, 270);
			seedBackground.graphics.endFill();
			seedBackground.x = 10;
			seedBackground.y = 30;
			seedBackground.alpha = 0.25;
			addChild(seedBackground);
			
			// Seed input
			seed = new TextField();
			seed.defaultTextFormat = inputTextFormat;
			seed.multiline = true;
			seed.type = TextFieldType.INPUT;
			seed.x = 10;
			seed.y = 30;
			seed.width = 200;
			seed.height = 270;		
			addChild(seed);
			seed.text = "Andor\rBaatar\rDrogo\rGrog\rGruumsh\rGrunt\rHodor\rHrothgar\rHrun\rKorg\rLothar\rOdin\rThor\rYngvar\rXandor";
			seed.addEventListener(FocusEvent.FOCUS_IN, handleSeedFocus);
			
			// Seed initial namegen
			saveSeed();
		}
		
		
		public function handleGenerateClick(event:MouseEvent):void
		{
			// Save seed if blocker is up
			if(blocker.visible)
			{
				blocker.visible = false;
				generateButtonLabel.text = "Generate";
				
				saveSeed();
			}
			else
			{
				generate();
			}
		}
		
		
		public function handleSeedFocus(event:FocusEvent):void
		{
			blocker.visible = true;
			generateButtonLabel.text = "Save Seed";
		}
		
		
		public function saveSeed():void
		{
			namegen = new WeightedLetterNamegen(seed.text.split("\r"));
		}
		
		
		public function generate():void
		{
			var names:Array = namegen.generate(10);
			results.text = names.join("\r");
		}
	}
}
