#include <stdio.h>

struct fib_input {
    short a;
};

struct fib_output {
    short a;
};

struct not_used {
    struct fib_input *a;
    struct fib_output *b;
    short c;
    int d;
};

struct not_used *init$not_used();

int main(int argc, char **argv) {
    struct not_used *not_used = init$not_used();
    printf("not_used {\n\ta: fib_input {\n\t\ta: %d\n\t}\n\tb: fib_output {\n\t\ta: %d\n\t}\n\tc: %d\n\td: %d\n}\n", not_used->a->a, not_used->b->a, not_used->c, not_used->d);

    return 0;
}