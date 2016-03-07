#`BlurPane`

This is a Java/Swing based component designed to produce a blurred overlay of the
parent container.

![Static component](https://cloud.githubusercontent.com/assets/10276932/13559010/fb57fc50-e461-11e5-8df1-990e13ebfe5b.gif)
![Dynamic components](https://cloud.githubusercontent.com/assets/10276932/13559019/2afe7b46-e462-11e5-855b-44245118bd27.gif)

The intention is for the component to generate a snapshot of the parent container,
of the area that the component covers, and generate a blurred image which it then
uses as it's own background to "simulate" the blurring affect.

While this can be done in a number of ways (including using `JComponent#print{All}`)
the requirement called for the background to remain dynamic, to allow the parent
component to continue to update and it would continue to blur those updates.

It was also required that no special container would be needed to generate the 
effect, the child component would deal with it.

*This is a hack* - There's no simpler way to put it. Because of the way painting
works in Swing, it was necessary to re-implement the painting process so that 
the parent container could be painted in such away that it would exclude the
child component and which could be done from within a current paint cycle.

While I've explored some optimization, there is still a lot of work to be done,
for example, when taking a snapshot of the parent container, it would, generally,
be more efficient to only capture the area which the component actually covered,
but I had issues when trying to implement this and haven't figured out how to
get around it.
