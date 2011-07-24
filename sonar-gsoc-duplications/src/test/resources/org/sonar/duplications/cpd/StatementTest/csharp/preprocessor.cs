#define DEBUG
#define VC_V7

#if (DEBUG && !VC_V7)
        //Console.WriteLine("DEBUG is defined");
#elif (!DEBUG && VC_V7)
        //Console.WriteLine("VC_V7 is defined");
#else
        //Console.WriteLine("DEBUG and VC_V7 are not defined");
#endif