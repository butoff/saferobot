NAME := Game
IN := in

.PHONY: run

run: $(NAME).class
	java $(NAME) < $(IN)

$(NAME).class: $(NAME).java
	javac $<

.PHONY: clean

clean:
	$(RM) *.class
