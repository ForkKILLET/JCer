# IceLava Text Picture and Animation File Format

## Instructions

An instruction is beginning with a `\`, and a char (see the table below) follows after.  

**Blank chars are necessary around instructions and their params.**  

When there is a newline at the end of a line (record line), it will be passed. To make a blank line at this time, add one more newline.

| Instruction name | Param(s) or null         | Instruction description     | Available file types   |
| :--------------- | :----------------------- | :-------------------------- | :--------------------- |
| `m`              | an int                   | Set the width               | `.pic.txt`, `.ani.txt` |
| `n`              | an int                   | Set the height              | `.pic.txt`, `.ani.txt` |
| `f`              | an int                   | Set the number of frames    | `.ani.txt`             |
| `D`              | a str                    | Set printing `direction`^1  |   `.pic.txt`, `.ani.txt` |
| `c`              | two `color hex`^2        | Set the color (in a frame)  | `.pic.txt`, `.ani.txt` |
| `C`              | a str and two color hex  | Set color of a specific str | `.pic.txt`, `.ani.txt` |
| `d`^3            | an int (and str) or null | Divide adjacent frames      | `.ani.txt`             |
| `e`              | null                     | End the line with spaces    | `.pic.txt`, `.ani.txt` |
| `r`^4            | an int or null           | Repeat a text several times | `.pic.txt`, `.ani.txt` |
| `R`              | a str or null            | Repeat a frame              | `.ani.txt`             |
| `s`^5            | a str                    | **Record** chars      | `.pic.txt`, `.ani.txt` |
| `u`              | four Unicode hex         | `Support`^6 hex escapes.    | `.pic.txt`, `.ani.txt` |
| `E`              | null                     | End of the file             | `.pic.txt`, `.ani.txt` |
| `b`              | null                     | Space escape                | `.pic.txt`, `.ani.txt` |
| `\` | null | Just a slash escape | `.pic.txt`, `.ani.txt` |

1. `x` or `y`.
2. We use two hex characters (foreground and background) to describe a color.

| Color name | hex  | hex with highlight |
| ---------: | ---: | ---: |
| black      | `0`  | `8`  |
| red        | `1`  | `9`  |
| green      | `2`  | `A`  |
| yellow     | `3`  | `B`  |
| blue       | `4`  | `C`  |
| magenta    | `5`  | `D`  |
| cyan       | `6`  | `E`  |
| white      | `7`  | `F`  |

So `0A` means light green foreground on black background.  
In particular, we use `=` to leave the color unchanged (same as the last `\c` instruction). There are some examples: `=6`, `F=`, `==` (It's useful to keep the color in the last frame. Or the color will be reset to the default `8F`.)  

**Highlighted background color is not supported now and won’t be supported in \*nix OS.**

`\C` instruction is extension of `\c`. Use cases: `\C JCer EF` and `\C ! 9=` .  

Attention, here `=` means the now using color instead of the last color set by `\c`.

2. A frame will first delay for several millisecond before showing.  

  Default 

  We can assign the name of a frame, like `\d 10 you can't catch me!`

3. `\r` instructions **appear in pairs**. It's like a loop.  
    For example, `\r 5 \c 9= Ice \c C= Lava \e \r` prints 5 lines of IceLava (with correct color :)  
    Specially, in `.ani.txt` files, a not paired `\r` instruction repeat until the end of frame (marked by `\d` instruction).  
    `\R` is a similar instruction, but it repeats a whole frame. When we don't assign the frame name, it repeats the last.
4. `\s` instructions record normal chars following until meeting another instruction (`\d ` or `\E`).
5. Only in `\s` instruction.

## Structure

The file should begin with with a `\m` instruction and then a `\n` one.