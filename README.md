<div align="center">

# TaglessFinalTesting


<img alt="Release date" src="https://img.shields.io/badge/release date-april 2023-red">
<img alt="GitHub last commit (by committer)" src="https://img.shields.io/github/last-commit/dhoang-creator/TaglessFinalTesting">
<img alt="Release date" src="https://img.shields.io/badge/dependenices-to upate-blue">
<img alt="License: MIT" src="https://img.shields.io/badge/License-MIT-yellow.svg" />


Tagless Final Testing is a template allowing Functional Programmers to test their Effectual code via the 'Functional Core, Imperative Shell' Testing Pattern.

Although originally conceived to be a Dependency Injection Pattern to resolve the Expression Problem found within FP, Tagless Final's structure of Algebras, Interpreters and Programs allow for a modular approach to testing.

  
## <a name='TOC'>Table of Contents</a>

- [Introduction](#introduction)
    - [Use Cases](#use_cases)

- [Getting Started](#getting_started)

- [Credits](#credits)
    - [Licence](#licence)

</div>

## Use Cases & Context

Tagless Final is a Dependency Injection tool which allows us to embed a DSL into a host language through the use of interfaces and algebras which are specific to the domain we want to model.

Alongside being a modular Dependency Injection tool, Tagless Final's DSL structure allows us to test the side effectual code by following the 'Imperative Shell, Functional Core' API building and testing pattern.

When attempting to build purely functional applications which interact with external dependencies/systems i.e. databases, we encapsulate the operation description of side effectual code in declarative statements such as 'Effects'.

By pushing the business logic to the centre of the application ('Functional Core'), we can utilise Unit Testing of such functions but we also need to test the code that sits on the edge of the application i.e. the side effectual Imperative Shell.

And Tagless Final allows us to model this. 

## Getting Started

Introduction to format and layout of the project.

## Credits

👤 **Duy Hoang**

* Github: [@dhoang-creator](https://github.com/dhoang-creator)
* LinkedIn: [@https:\/\/www.linkedin.com\/in\/duy-hoang-155930262\/](https://linkedin.com/in/https:\/\/www.linkedin.com\/in\/duy-hoang-155930262\/)

## License

Copyright (c) 2023 - 2033 Duy Hoang and others

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
