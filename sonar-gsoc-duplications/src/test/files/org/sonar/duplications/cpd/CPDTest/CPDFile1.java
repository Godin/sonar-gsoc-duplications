public class DirListing {
  public static void main(String[] args) {
    switch (args.length) {
      case 0:
        System.out.println("Directory has not mentioned.");
        System.exit(0);
      case 1:
        dirlist(args[0]);
        System.exit(0);
      default:
        System.out.println("Multiple files are not allow.");
        System.exit(0);
    }
  }

  public void smallClone() {
    int n = 100;
    for (int i = 0; i < n,i++)
    printf(i * n);
  }
}