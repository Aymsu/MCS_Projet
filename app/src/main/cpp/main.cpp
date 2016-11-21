#include <jni.h>
#include <string>
#include "dtw.h"
#include "WavToMfcc.h"
#include <stdio.h>
#include <stdlib.h>
#include <sstream>
#include <iostream>
#include <cstdlib>
#include <cstdio>
#include <cstring>
#include <vector>
#include <limits>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <android/log.h>
#include <pthread.h>

#define APPNAME "MCS"

#define nbMots 9
#define nbLocuteurs 8

std::string vocabulaire[] = {"arretetoi",  "avance", "droite", "etatdurgence", "faisunflip", "gauche", "recule", "tournedroite", "tournegauche"};
std::string locuteurs[] = {"M01", "M02","M03","M04", "M05","M06","M07","M08", "M09", "M10", "M11", "M12", "M13", "F02","F03","F04", "F05"};

AAssetManager * manager;

using namespace std;

int best[nbLocuteurs];
pthread_t threads[nbLocuteurs];

struct parametres{
    int indice;
    std::string locuteur;
    int size;
    float * buffer;
};

char * fichier(std::string ordre, std::string locuteur){
    std::stringstream ss;
    ss << "corpus/dronevolant_nonbruite/" << locuteur << "_" << ordre << ".wav";
    __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "%s", ss.str().c_str());
    //printf("%s\n", ss.str().c_str());
    return strdup(ss.str().c_str());
}

/**
* Read a wave file.
*
* @param p_wav (OUT) pointer to a file descriptor
* @param filename (IN) pointer to the name of the file
* @param p_header (OUT) pointer to a wave header structure
* @return none
*/
void wavReadFomAsset(AAsset **p_wav, char *filename, wavfile *p_header) {
    //fopen_s(p_wav, filename, "rb"); //Windows version
    *p_wav = AAssetManager_open(manager, filename, AASSET_MODE_UNKNOWN);
    if (*p_wav == NULL) {
        fprintf(stderr, "Can't open input file %s\n", filename);
        exit(1);
    }

    // read header
    if (AAsset_read(*p_wav, p_header, sizeof(wavfile)) < 1) {
        fprintf(stderr, "Can't read input file header %s\n", filename);
        exit(1);
    }

    // if wav file isn't the same endianness than the current environment
    // we quit
    if (is_big_endian()) {
        if (memcmp((*p_header).id, "RIFX", 4) != 0) {
            fprintf(stderr, "ERROR: %s is not a big endian wav file\n", filename);
            exit(1);
        }
    }
    else {
        if (memcmp((*p_header).id, "RIFF", 4) != 0) {
            fprintf(stderr, "ERROR: %s is not a little endian wav file\n", filename);
            exit(1);
        }
    }

    if (memcmp((*p_header).wavefmt, "WAVEfmt ", 8) != 0
        || memcmp((*p_header).data, "data", 4) != 0
            ) {
        fprintf(stderr, "ERROR: Not wav format\n");
        exit(1);
    }
    if ((*p_header).format != 16) {
        fprintf(stderr, "\nERROR: not 16 bit wav format.");
        exit(1);
    }
    if (memcmp((*p_header).data, "data", 4) != 0) {
        fprintf(stderr, "ERROR: Prrroblem?\n");
        exit(1);
    }
}

void getMyMFCC(char * filename, float *** buffer, int * size){
    struct wavfile mywav;
    FILE * f;

    __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "%s", filename);

    *buffer = new float*; 

    wavRead(&f, filename, &mywav);

    int16_t ** bufferSilence = new int16_t*;
    int newLength;
    int16_t * wavbuffer;

    wavbuffer = new int16_t[mywav.bytes_in_data];
    int16_t b;
    
    int i = 0;
	while(fread(&b, sizeof(int16_t), 1, f) > 0){
		wavbuffer[i++] = b;
	}

    __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "size : %d ", mywav.bytes_in_data);

   //int n = fread(wavbuffer, sizeof(int16_t), mywav.bytes_in_data/sizeof(int16_t), f);

    //removeSilence(wavbuffer, mywav.bytes_in_data/sizeof(int16_t), bufferSilence, &newLength, 10);
    //delete(wavbuffer);

    //computeMFCC(*buffer, size, * bufferSilence, newLength, mywav.frequency, 512, 256, 13, 26);
    computeMFCC(*buffer, size, wavbuffer, mywav.bytes_in_data/sizeof(int16_t), mywav.frequency, 512, 256, 13, 26);
    //delete[] wavbuffer, f;

    __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "%d", *size);
}

void getMyMFCCFromAssets(char * filename, float *** buffer, int * size){
    struct wavfile mywav;
    AAsset * f;

    *buffer = new float*;

    wavReadFomAsset(&f, filename, &mywav);

    //int16_t ** bufferSilence = new int16_t*;
    //int newLength;
    int16_t * wavbuffer;

    wavbuffer = new int16_t[mywav.bytes_in_data];
    int16_t b;

    int i = 0;
    while(AAsset_read(f, &b, sizeof(int16_t)) > 0){
        wavbuffer[i++] = b;
    }

    //int n = fread(wavbuffer, sizeof(int16_t), mywav.bytes_in_data/sizeof(int16_t), f);

    //removeSilence(wavbuffer, mywav.bytes_in_data/sizeof(int16_t), bufferSilence, &newLength, 10);
    //delete(wavbuffer);

    //computeMFCC(*buffer, size, * bufferSilence, newLength, mywav.frequency, 512, 256, 13, 26);
    computeMFCC(*buffer, size, wavbuffer, mywav.bytes_in_data/sizeof(int16_t), mywav.frequency, 512, 256, 13, 26);
    //delete[] wavbuffer, f;
}

void * findBest(void * params ){

    struct parametres p = *((struct parametres*)params);

    float inf = std::numeric_limits<float>::infinity();
    float min = inf;
    int indiceMin = 0;
    for (int j = 0; j < nbMots; ++j)
    {
        float ** buffLoc;
        int sizeLoc;
        getMyMFCCFromAssets(fichier(vocabulaire[j], p.locuteur), &buffLoc, &sizeLoc);
        float cout = dtw(sizeLoc, p.size, 13, *buffLoc, p.buffer);
        //__android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "%f", cout);
        if(cout < min){
            min = cout;
            indiceMin = j;
        }
        delete(*buffLoc);
        delete(buffLoc);
    }
    best[p.indice] = indiceMin;
    //__android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "%d", indiceMin);
    return NULL;
}

int findWord(char * wavfile){
    int MatriceConfusion[nbMots] = {0};
    float ** buffWav;
    int sizeWav;
    getMyMFCC(wavfile, &buffWav, &sizeWav);
    __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "Running ...");
    struct parametres p;
    p.buffer = *buffWav;
    p.size = sizeWav;
    for (int i = 0; i < nbLocuteurs; ++i)
    {
        p.indice = i;
        p.locuteur = locuteurs[i];
        pthread_create(&threads[i], NULL, findBest, &p);
    }
    for (int i = 0; i < nbLocuteurs; ++i) {
        pthread_join(threads[i], NULL);
        MatriceConfusion[best[i]] ++;
    }
    int max = 0;
    int indiceMax = 0;
    for (int i = 0; i < nbMots; ++i)
    {
        if(MatriceConfusion[i] >= max){
            max = MatriceConfusion[i];
            indiceMax = i;
        }
        __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "%d", MatriceConfusion[i]);
        //printf("%d\n", MatriceConfusion[i]);
    }
    return indiceMax;
}

extern "C"

JNIEXPORT jstring JNICALL
Java_com_paulsab_aymer_mcs_RecoVocale_recoVocale(JNIEnv *env, jobject instance,
                                                 jstring filename_, jobject manager_) {
    const char *filename = env->GetStringUTFChars(filename_, 0);
    manager = AAssetManager_fromJava(env, manager_);

    //std::string locuteur = "Aym";
    //char * filename = fichier(vocabulaire[4], locuteur);
    //char * test = fichier(vocabulaire[0], "M01");

    //printf("Mot a reconnaitre : %s\n", vocabulaire[4].c_str());

    //TODO Adpater l'adresse du corpus

    int indMot = findWord(strdup(filename));
    //printf("L'ordre reconnu est : %s\n", vocabulaire[indMot].c_str());
    //printf("cout : %f\n", cout);
    return env->NewStringUTF(vocabulaire[indMot].c_str());
    //return 0;
}

/*int main(int argc, char const *argv[])
{
    std::string vocabulaire[] = {"arretetoi", "atterrissage", "avance", "decollage", "droite", "etatdurgence", "faisunflip", "gauche", "plusbas", "plushaut", "recule", "tournedroite", "tournegauche"};
    std::string locuteur = "Aym";
    char * filename = fichier(vocabulaire[0], locuteur);
    wavfile mywav;
    FILE * f;
    dtw(0,0,0, NULL, NULL);
    //wavRead(&f, filename, &mywav);
    return 0;
}*/
