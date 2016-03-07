#`BlurPane`

This is a Java/Swing based component designed to produce a blurred overlay of the
parent container.

The intention was to design a "transparent popup style component which blurred 
the area of the container it covered", as you might see in some mobile platforms

![Static component](https://cloud.githubusercontent.com/assets/10276932/13559010/fb57fc50-e461-11e5-8df1-990e13ebfe5b.gif)
![Dynamic components](https://cloud.githubusercontent.com/assets/10276932/13559019/2afe7b46-e462-11e5-855b-44245118bd27.gif)

The above examples are intended to push the implementation to see what it could
do. In my testing, I can about 10 `BlurPane`s moving around the screen, but the
frame rate drops dramatically

The intention is for the component to generate a snapshot of the parent container,
of the area that the component covers, and generate a blurred image which it then
used as it's own background to "simulate" the blurring affect.

While this can be done in a number of ways (including using `JComponent#print{All}`)
the requirement called for the background to remain dynamic, to allow the parent
component to continue to update and continue to be "blurred"

It was also required that no special container would be needed to generate the 
effect, the child component would deal with it.

*This is a hack* - There's no simpler way to put it. Because of the way painting
works in Swing, it was necessary to re-implement the painting process so that 
the parent container could be painted in such away that it would exclude the
child component and which could be done from within a current paint cycle.

While I've explored some optimization, there is still a lot of work to be done,
for example, when taking a snapshot of the parent container, it would, generally,
be more efficient to only capture the area that the component actually covered,
but I had issues when trying to implement this and haven't figured out how to
get around it.

#Personal library code

The repository includes a number of personal libraries bundled in the lib
directory of the project. These libraries are required for demos and aren't
part of the implementation.  It should be possible to take the core implementation
and use it without these libraries

I choose not to place the project into a Maven structure as the libraries used
are actually part of private/personal Maven repository as well, which would have
prevented anyone from downloading them

I could have just dumped all the source code needed into a single project, but 
that would have dumped a whole lot of code into the project which simply wasn't 
needed to demonstrate the concept