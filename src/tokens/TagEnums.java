/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tokens;

/**
 *
 * @author cristiano
 */
public class TagEnums {

    public static int //Palavras reservadas
            PROGRAM = 256,
            DECLARE = 257,
            BEGIN = 258,
            END = 259,
            INT = 260,
            FLOAT = 261,
            CHAR = 262,
            IF = 263,
            THEN = 264,
            ELSE = 265,
            REPEAT = 266,
            UNTIL = 267,
            WHILE = 268,
            DO = 269,
            IN = 270,
            OUT = 271,
            
            //Operadores e pontuação
            
            EQ = 272,
            GE = 273,
            GT = 274,
            LE = 275,
            LT = 276,
            NE = 277,
            ADD = 278,
            SUB = 279,
            MUL = 280,
            DIV = 281,
            AND = 282,
            OR = 283,
            ASSIGN = 284,
            NOT= 285,
            COMMA= 286,
            SEMICOLON= 287,
            COLON= 288,
            OPEN_BRA= 289,
            CLOSE_BRA= 290,
            DOT= 291,
            //Outros tokens
            
            NUM =292,
            ID = 293,
            STRING = 294,
            CARACTERE = 295,
            INVALID_TOKEN = 296,
            END_OF_FILE = 297,
            COMMENT = 298,
            UNEXPECTED_EOF = 299;
}
