NAME := Game
IN := in
OUT := out

.PHONY: run

run: $(NAME).class
	java $(NAME) < $(IN)

$(NAME).class: $(NAME).java
	javac $<

.PHONY: clean

clean:
	$(RM) *.class
